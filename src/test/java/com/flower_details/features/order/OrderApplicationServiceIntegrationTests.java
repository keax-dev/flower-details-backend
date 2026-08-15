package com.flower_details.features.order;

import com.flower_details.features.cart.application.dto.command.AddCartItemCommand;
import com.flower_details.features.cart.application.service.CartApplicationService;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.order.application.dto.command.CreateOrderCommand;
import com.flower_details.features.order.application.dto.view.OrderView;
import com.flower_details.features.order.application.service.OrderApplicationService;
import com.flower_details.features.order.domain.model.FulfillmentType;
import com.flower_details.features.order.domain.model.OrderStatus;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.repository.ProductRepository;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.shared.domain.DomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OrderApplicationServiceIntegrationTests {

	@Autowired
	private OrderApplicationService orderApplicationService;

	@Autowired
	private CartApplicationService cartApplicationService;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void createsAnOrderWithPriceSnapshotAndOnlyItsOperatorCanAdvanceIt() {
		String suffix = UUID.randomUUID().toString();
		User customer = userRepository.save(User.registerCustomer("customer-" + suffix + "@flowerdetails.test", "hash"));
		User assignedOperator = userRepository.save(User.createStaff("operator-a-" + suffix + "@flowerdetails.test", "hash", UserRole.OPERATOR));
		User anotherOperator = userRepository.save(User.createStaff("operator-b-" + suffix + "@flowerdetails.test", "hash", UserRole.OPERATOR));
		Category category = categoryRepository.save(Category.create("Pedidos " + suffix, "Categoria de prueba", true));
		Product product = productRepository.save(Product.create(
				category.id(), "Ramo " + suffix, "Detalle de prueba", new BigDecimal("24.50"), true
		));

		cartApplicationService.addItem(customer.id(), new AddCartItemCommand(product.id(), 2));
		OrderView order = orderApplicationService.create(customer.id(), new CreateOrderCommand(
				FulfillmentType.PICKUP, "Cliente", "0999999999", null, "Sin envoltura adicional"
		));

		assertThat(order.status()).isEqualTo(OrderStatus.GENERATED);
		assertThat(order.total()).isEqualByComparingTo("49.00");
		assertThat(order.items()).singleElement().satisfies(item -> {
			assertThat(item.unitPrice()).isEqualByComparingTo("24.50");
			assertThat(item.subtotal()).isEqualByComparingTo("49.00");
		});
		assertThat(cartApplicationService.getCart(customer.id()).items()).isEmpty();

		orderApplicationService.assign(order.id(), assignedOperator.id(), UserRole.OPERATOR, null);
		assertThatThrownBy(() -> orderApplicationService.changeStatus(
				order.id(), anotherOperator.id(), UserRole.OPERATOR, OrderStatus.IN_PREPARATION
		)).isInstanceOf(DomainException.class);

		OrderView inPreparation = orderApplicationService.changeStatus(
				order.id(), assignedOperator.id(), UserRole.OPERATOR, OrderStatus.IN_PREPARATION
		);
		assertThat(inPreparation.status()).isEqualTo(OrderStatus.IN_PREPARATION);
	}
}
