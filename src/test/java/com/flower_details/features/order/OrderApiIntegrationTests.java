package com.flower_details.features.order;

import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.repository.ProductRepository;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.features.users.domain.repository.PersonRepository;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.shared.infrastructure.security.BCryptPasswordService;
import com.flower_details.support.CsrfTestToken;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiIntegrationTests {

	private static final String ACCESS_COOKIE_NAME = "flower_details_test_access_token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private BCryptPasswordService passwordService;

	@Test
	void customerCanCheckoutAndAssignedOperatorCanCompleteTheOrderWithAuditTrail() throws Exception {
		String suffix = UUID.randomUUID().toString();
		Cookie customerCookie = registerCustomer("order-customer-" + suffix + "@flowerdetails.test");
		Cookie operatorCookie = createStaffAndLogin("order-operator-" + suffix + "@flowerdetails.test", UserRole.OPERATOR);
		CsrfTestToken csrfToken = CsrfTestToken.obtain(mockMvc);
		Product product = createProduct(suffix, new BigDecimal("32.50"));

		Long orderId = addToCartAndCreateOrder(customerCookie, csrfToken, product.id());

		mockMvc.perform(get("/api/orders/my").cookie(customerCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].id").value(orderId))
				.andExpect(jsonPath("$.items[0].total").value(32.50));

		mockMvc.perform(patch("/api/orders/{id}/status", orderId)
						.cookie(operatorCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"IN_PREPARATION\"}"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(patch("/api/orders/{id}/assign", orderId)
						.cookie(operatorCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ASSIGNED"));

		changeStatus(operatorCookie, csrfToken, orderId, "IN_PREPARATION");
		changeStatus(operatorCookie, csrfToken, orderId, "READY_FOR_DELIVERY");
		changeStatus(operatorCookie, csrfToken, orderId, "DELIVERED");

		mockMvc.perform(get("/api/orders").cookie(operatorCookie))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/orders/{id}", orderId).cookie(operatorCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DELIVERED"));

		mockMvc.perform(get("/api/orders/{id}/audit", orderId).cookie(customerCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(5))
				.andExpect(jsonPath("$[0].action").value("CREATED"))
				.andExpect(jsonPath("$[1].action").value("ASSIGNED"))
				.andExpect(jsonPath("$[2].currentStatus").value("IN_PREPARATION"))
				.andExpect(jsonPath("$[4].currentStatus").value("DELIVERED"));
	}

	@Test
	void rejectsCheckoutWithoutCsrfAndProtectsOrderOwnershipAndManagementActions() throws Exception {
		String suffix = UUID.randomUUID().toString();
		Cookie ownerCookie = registerCustomer("order-owner-" + suffix + "@flowerdetails.test");
		Cookie otherCustomerCookie = registerCustomer("order-other-" + suffix + "@flowerdetails.test");
		CsrfTestToken csrfToken = CsrfTestToken.obtain(mockMvc);
		Product product = createProduct(suffix, new BigDecimal("18.00"));

		mockMvc.perform(post("/api/cart/items")
						.cookie(ownerCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"productId\":%d,\"quantity\":1}".formatted(product.id())))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/orders")
						.cookie(ownerCookie)
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderPayload()))
				.andExpect(status().isForbidden());

		Long orderId = createOrder(ownerCookie, csrfToken);

		mockMvc.perform(get("/api/orders/{id}", orderId).cookie(otherCustomerCookie))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/orders/{id}/audit", orderId).cookie(otherCustomerCookie))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/orders").cookie(ownerCookie))
				.andExpect(status().isForbidden());
		mockMvc.perform(patch("/api/orders/{id}/status", orderId)
						.cookie(ownerCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"IN_PREPARATION\"}"))
				.andExpect(status().isForbidden());

		mockMvc.perform(patch("/api/orders/{id}/cancel", orderId)
						.cookie(ownerCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"reason\":\"El cliente cambio de opinion\"}"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/orders/{id}/audit", orderId).cookie(ownerCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[1].action").value("CANCELLED"))
				.andExpect(jsonPath("$[1].details").value("El cliente cambio de opinion"));
	}

	private Long addToCartAndCreateOrder(Cookie customerCookie, CsrfTestToken csrfToken, Long productId) throws Exception {
		mockMvc.perform(post("/api/cart/items")
						.cookie(customerCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"productId\":%d,\"quantity\":1}".formatted(productId)))
				.andExpect(status().isCreated());
		return createOrder(customerCookie, csrfToken);
	}

	private Long createOrder(Cookie customerCookie, CsrfTestToken csrfToken) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/orders")
						.cookie(customerCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderPayload()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("GENERATED"))
				.andExpect(jsonPath("$.items.length()").value(1))
				.andReturn();
		return readLong(result, "$.id");
	}

	private void changeStatus(Cookie operatorCookie, CsrfTestToken csrfToken, Long orderId, String status) throws Exception {
		mockMvc.perform(patch("/api/orders/{id}/status", orderId)
						.cookie(operatorCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"%s\"}".formatted(status)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(status));
	}

	private Product createProduct(String suffix, BigDecimal price) {
		Category category = categoryRepository.save(Category.create("Pedidos API " + suffix, "Categoria de pruebas", true));
		return productRepository.save(Product.create(category.id(), "Ramo API " + suffix, "Producto para pedidos", price, true));
	}

	private Cookie registerCustomer(String email) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "names": "Cliente",
								  "lastNames": "Pedidos",
								  "email": "%s",
								  "password": "Password123"
								}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andReturn();
		return requireAccessCookie(result);
	}

	private Cookie createStaffAndLogin(String email, UserRole role) throws Exception {
		String password = "Password123";
		User staff = userRepository.save(User.createStaff(email, passwordService.hash(password), role));
		personRepository.save(Person.create(staff.id(), "Equipo", "Pedidos", null, null));

		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
				.andExpect(status().isOk())
				.andReturn();
		return requireAccessCookie(result);
	}

	private static Cookie requireAccessCookie(MvcResult result) {
		Cookie cookie = result.getResponse().getCookie(ACCESS_COOKIE_NAME);
		assertThat(cookie).isNotNull();
		return cookie;
	}

	private static Long readLong(MvcResult result, String jsonPath) throws Exception {
		Object value = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), jsonPath);
		return ((Number) value).longValue();
	}

	private static String orderPayload() {
		return """
				{
				  "fulfillmentType": "PICKUP",
				  "contactName": "Cliente Pedidos",
				  "contactPhone": "0999999999",
				  "additionalInstructions": "Entregar con tarjeta"
				}
				""";
	}
}
