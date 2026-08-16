package com.flower_details.features.cart;

import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.repository.ProductRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartIntegrationTests {

	private static final String ACCESS_COOKIE_NAME = "flower_details_test_access_token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductRepository productRepository;

	@Test
	void customerCanManageCartAndKeepsTheOriginalUnitPrice() throws Exception {
		String suffix = UUID.randomUUID().toString();
		Cookie customerCookie = registerCustomer(suffix);
		CsrfTestToken csrfToken = CsrfTestToken.obtain(mockMvc);
		Category category = categoryRepository.save(Category.create("Carrito " + suffix, "Categoria de carrito", true));
		Product product = productRepository.save(Product.create(
				category.id(), "Ramo " + suffix, "Detalle para el carrito", new BigDecimal("29.90"), true
		));

		mockMvc.perform(post("/api/cart/items")
						.cookie(customerCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"productId\":%d,\"quantity\":1}".formatted(product.id())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.items[0].quantity").value(1))
				.andExpect(jsonPath("$.items[0].unitPrice").value(29.90))
				.andExpect(jsonPath("$.total").value(29.90));

		product.update(category.id(), product.title(), product.description(), new BigDecimal("45.00"), true);
		productRepository.save(product);

		MvcResult addAgainResult = mockMvc.perform(post("/api/cart/items")
						.cookie(customerCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"productId\":%d,\"quantity\":2}".formatted(product.id())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.items[0].quantity").value(3))
				.andExpect(jsonPath("$.items[0].unitPrice").value(29.90))
				.andExpect(jsonPath("$.items[0].subtotal").value(89.70))
				.andExpect(jsonPath("$.total").value(89.70))
				.andReturn();

		Long itemId = readLong(addAgainResult, "$.items[0].id");
		mockMvc.perform(put("/api/cart/items/{itemId}", itemId)
						.cookie(customerCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"quantity\":4}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(119.60));

		mockMvc.perform(delete("/api/cart/items/{itemId}", itemId)
						.cookie(customerCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/cart").cookie(customerCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isEmpty())
				.andExpect(jsonPath("$.total").value(0));
	}

	@Test
	void nonCustomerCannotAccessCart() throws Exception {
		mockMvc.perform(get("/api/cart"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void customerCanRemoveAProductThatIsNoLongerAvailable() throws Exception {
		String suffix = UUID.randomUUID().toString();
		Cookie customerCookie = registerCustomer(suffix);
		CsrfTestToken csrfToken = CsrfTestToken.obtain(mockMvc);
		Category category = categoryRepository.save(Category.create("No disponible " + suffix, "Categoria de carrito", true));
		Product product = productRepository.save(Product.create(
				category.id(), "Ramo agotado " + suffix, "Detalle para el carrito", new BigDecimal("29.90"), true
		));

		MvcResult addResult = mockMvc.perform(post("/api/cart/items")
						.cookie(customerCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"productId\":%d,\"quantity\":1}".formatted(product.id())))
				.andExpect(status().isCreated())
				.andReturn();

		Long itemId = readLong(addResult, "$.items[0].id");
		product.deactivate();
		productRepository.save(product);

		mockMvc.perform(get("/api/cart").cookie(customerCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].product.id").value(product.id()))
				.andExpect(jsonPath("$.items[0].product.available").value(false));

		mockMvc.perform(delete("/api/cart/items/{itemId}", itemId)
						.cookie(customerCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token()))
				.andExpect(status().isNoContent());
	}

	private Cookie registerCustomer(String suffix) throws Exception {
		String email = "cart-" + suffix + "@flowerdetails.test";
		MvcResult result = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "names": "Cliente",
								  "lastNames": "Carrito",
								  "email": "%s",
								  "password": "Password123"
								}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andReturn();
		return result.getResponse().getCookie(ACCESS_COOKIE_NAME);
	}

	private static Long readLong(MvcResult result, String jsonPath) throws Exception {
		Object value = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), jsonPath);
		return ((Number) value).longValue();
	}
}
