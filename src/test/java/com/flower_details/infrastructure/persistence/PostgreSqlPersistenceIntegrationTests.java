package com.flower_details.infrastructure.persistence;

import com.flower_details.features.cart.domain.model.Cart;
import com.flower_details.features.cart.domain.repository.CartRepository;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.order.domain.model.FulfillmentType;
import com.flower_details.features.order.domain.model.Order;
import com.flower_details.features.order.domain.repository.OrderRepository;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.features.users.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class PostgreSqlPersistenceIntegrationTests {

	@Container
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("flower_details_test")
			.withUsername("flower_details")
			.withPassword("flower_details");

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@DynamicPropertySource
	static void configurePostgreSql(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
		registry.add("spring.flyway.enabled", () -> true);
		registry.add("app.bootstrap.admin.enabled", () -> false);
	}

	@Test
	void flywayCreatesSchemaAndPartialIndexes() {
		assertThat(tableExists("users")).isTrue();
		assertThat(tableExists("carts")).isTrue();
		assertThat(tableExists("orders")).isTrue();
		assertThat(partialIndexPredicate("uk_carts_customer_active"))
				.contains("deleted_at IS NULL")
				.contains("status");
		assertThat(partialIndexPredicate("uk_categories_title_active")).contains("deleted_at IS NULL");
	}

	@Test
	void softDeleteUsesTimestampColumnInPostgreSql() {
		Category category = categoryRepository.save(Category.create("PostgreSQL soft delete", "Categoria de prueba", true));
		categoryRepository.delete(category);

		Integer deletedRows = jdbcTemplate.queryForObject(
				"select count(*) from categories where id = ? and deleted_at is not null",
				Integer.class,
				category.id()
		);
		assertThat(deletedRows).isEqualTo(1);
	}

	@Test
	void pessimisticCartLockBlocksAnotherTransactionUntilReleased() throws Exception {
		User customer = userRepository.save(User.registerCustomer("postgres-lock@flowerdetails.test", "hash"));
		Cart cart = transactionTemplate.execute(status -> cartRepository.save(Cart.create(customer.id())));
		assertThat(cart).isNotNull();

		CountDownLatch firstLockAcquired = new CountDownLatch(1);
		CountDownLatch releaseFirstTransaction = new CountDownLatch(1);
		CountDownLatch secondLockAcquired = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);

		try {
			Future<?> firstTransaction = executor.submit(() -> transactionTemplate.executeWithoutResult(status -> {
				cartRepository.findActiveByCustomerIdForUpdate(customer.id()).orElseThrow();
				firstLockAcquired.countDown();
				await(releaseFirstTransaction);
			}));

			assertThat(firstLockAcquired.await(5, TimeUnit.SECONDS)).isTrue();
			Future<?> secondTransaction = executor.submit(() -> transactionTemplate.executeWithoutResult(status -> {
				cartRepository.findActiveByCustomerIdForUpdate(customer.id()).orElseThrow();
				secondLockAcquired.countDown();
			}));

			assertThat(secondLockAcquired.await(300, TimeUnit.MILLISECONDS)).isFalse();
			releaseFirstTransaction.countDown();
			assertThat(secondLockAcquired.await(5, TimeUnit.SECONDS)).isTrue();
			firstTransaction.get(5, TimeUnit.SECONDS);
			secondTransaction.get(5, TimeUnit.SECONDS);
		}
		finally {
			releaseFirstTransaction.countDown();
			executor.shutdownNow();
		}
	}

	@Test
	void optimisticOrderLockRejectsAStaleUpdate() {
		User customer = userRepository.save(User.registerCustomer("postgres-order-customer@flowerdetails.test", "hash"));
		User operator = userRepository.save(User.createStaff(
				"postgres-order-operator@flowerdetails.test", "hash", UserRole.OPERATOR
		));
		Order saved = transactionTemplate.execute(status -> orderRepository.save(Order.create(
				"FD-POSTGRES-VERSION", customer.id(), FulfillmentType.PICKUP, java.math.BigDecimal.TEN,
				"Cliente", "0999999999", null, null
		)));
		assertThat(saved).isNotNull();

		Order firstCopy = transactionTemplate.execute(status -> orderRepository.findById(saved.id()).orElseThrow());
		Order staleCopy = transactionTemplate.execute(status -> orderRepository.findById(saved.id()).orElseThrow());

		firstCopy.assignTo(operator.id(), java.time.Instant.now());
		transactionTemplate.executeWithoutResult(status -> orderRepository.save(firstCopy));

		staleCopy.assignTo(operator.id(), java.time.Instant.now());
		assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> orderRepository.save(staleCopy)))
				.isInstanceOf(ObjectOptimisticLockingFailureException.class);
	}

	private boolean tableExists(String tableName) {
		Boolean exists = jdbcTemplate.queryForObject(
				"select to_regclass('public.' || ?) is not null",
				Boolean.class,
				tableName
		);
		return Boolean.TRUE.equals(exists);
	}

	private String partialIndexPredicate(String indexName) {
		return jdbcTemplate.queryForObject(
				"""
						select pg_get_expr(index_definition.indpred, index_definition.indrelid)
						from pg_index index_definition
						join pg_class index_class on index_class.oid = index_definition.indexrelid
						where index_class.relname = ?
						""",
				String.class,
				indexName
		);
	}

	private static void await(CountDownLatch latch) {
		try {
			if (!latch.await(10, TimeUnit.SECONDS)) {
				throw new IllegalStateException("Se agoto el tiempo esperando la transaccion bloqueada");
			}
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("La transaccion fue interrumpida", exception);
		}
	}
}
