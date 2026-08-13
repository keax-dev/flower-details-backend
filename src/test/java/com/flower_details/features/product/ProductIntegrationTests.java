package com.flower_details.features.product;

import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.product.application.port.out.ProductRepositoryPort;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.users.application.port.out.PersonRepositoryPort;
import com.flower_details.features.users.application.port.out.UserRepositoryPort;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.shared.security.PasswordHasher;
import com.flower_details.support.CsrfTestToken;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductIntegrationTests {

	private static final String ACCESS_COOKIE_NAME = "flower_details_test_access_token";
	private static final byte[] FIRST_IMAGE = new byte[] {
			(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3, 4
	};
	private static final byte[] SECOND_IMAGE = new byte[] {
			0x52, 0x49, 0x46, 0x46, 1, 2, 3, 4, 0x57, 0x45, 0x42, 0x50
	};

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductRepositoryPort productRepository;

	@Autowired
	private UserRepositoryPort userRepository;

	@Autowired
	private PersonRepositoryPort personRepository;

	@Autowired
	private PasswordHasher passwordHasher;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void adminCanCreateUpdateReadAndSoftDeleteProductWithImages() throws Exception {
		String suffix = UUID.randomUUID().toString();
		Cookie adminCookie = createAdminAndLogin(suffix);
		CsrfTestToken csrfToken = CsrfTestToken.obtain(mockMvc);
		Category category = categoryRepository.save(Category.create(
				"Rosas " + suffix,
				"Arreglos con rosas para ocasiones especiales",
				true
		));

		MvcResult createResult = mockMvc.perform(multipart("/api/products")
						.file(image("images", "ramo.png", FIRST_IMAGE))
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.param("categoryId", category.id().toString())
						.param("title", "Ramo premium " + suffix)
						.param("description", "Ramo floral premium")
						.param("price", "29.90")
						.param("active", "true"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Ramo premium " + suffix))
				.andExpect(jsonPath("$.category.id").value(category.id()))
				.andExpect(jsonPath("$.images[0].contentType").value("image/png"))
				.andReturn();

		Long productId = readLong(createResult, "$.id");
		String firstImageUrl = readString(createResult, "$.images[0].url");

		mockMvc.perform(get("/api/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(productId));

		mockMvc.perform(get("/api/products/{id}", productId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.price").value(29.90));

		mockMvc.perform(get(firstImageUrl))
				.andExpect(status().isOk())
				.andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(FIRST_IMAGE));

		MockMultipartHttpServletRequestBuilder updateRequest = multipart("/api/products/{id}", productId);
		updateRequest.with(request -> {
			request.setMethod("PUT");
			return request;
		});

		MvcResult updateResult = mockMvc.perform(updateRequest
						.file(image("images", "actualizado.webp", SECOND_IMAGE))
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.param("categoryId", category.id().toString())
						.param("title", "Ramo actualizado " + suffix)
						.param("description", "Ramo floral actualizado")
						.param("price", "35.50")
						.param("active", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Ramo actualizado " + suffix))
				.andExpect(jsonPath("$.images[0].contentType").value("image/webp"))
				.andReturn();

		String secondImageUrl = readString(updateResult, "$.images[0].url");

		mockMvc.perform(get(firstImageUrl))
				.andExpect(status().isNotFound());
		mockMvc.perform(get(secondImageUrl))
				.andExpect(status().isOk())
				.andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(SECOND_IMAGE));

		mockMvc.perform(delete("/api/products/{id}", productId)
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token()))
				.andExpect(status().isNoContent());

		assertThat(productRepository.findById(productId)).isEmpty();
		assertThat(countProductRowsById(productId)).isEqualTo(1L);
		assertThat(countSoftDeletedProductRowsById(productId)).isEqualTo(1L);
		assertThat(countSoftDeletedImageRowsByProductId(productId)).isGreaterThanOrEqualTo(2L);

		mockMvc.perform(get("/api/products/{id}", productId))
				.andExpect(status().isNotFound());
		mockMvc.perform(get(secondImageUrl))
				.andExpect(status().isNotFound());
	}

	@Test
	void anonymousUserCannotCreateProduct() throws Exception {
		mockMvc.perform(multipart("/api/products")
						.file(image("images", "sin-permiso.png", FIRST_IMAGE))
						.param("categoryId", "1")
						.param("title", "Sin permiso")
						.param("description", "No deberia crearse")
						.param("price", "10.00")
						.param("active", "true"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void cookieAuthenticatedUserNeedsCsrfTokenToCreateProduct() throws Exception {
		String suffix = UUID.randomUUID().toString();
		Cookie adminCookie = createAdminAndLogin(suffix);
		Category category = categoryRepository.save(Category.create("CSRF " + suffix, "Categoria de prueba", true));

		mockMvc.perform(multipart("/api/products")
						.file(image("images", "ramo.png", FIRST_IMAGE))
						.cookie(adminCookie)
						.param("categoryId", category.id().toString())
						.param("title", "Sin CSRF")
						.param("description", "No debe crearse")
						.param("price", "10.00"))
				.andExpect(status().isForbidden());
	}

	@Test
	void rejectsFileWhoseContentDoesNotMatchDeclaredImageType() throws Exception {
		String suffix = UUID.randomUUID().toString();
		Cookie adminCookie = createAdminAndLogin(suffix);
		CsrfTestToken csrfToken = CsrfTestToken.obtain(mockMvc);
		Category category = categoryRepository.save(Category.create("Imagen invalida " + suffix, "Categoria de prueba", true));

		mockMvc.perform(multipart("/api/products")
						.file(new MockMultipartFile("images", "falso.png", "image/png", new byte[] {1, 2, 3, 4}))
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.param("categoryId", category.id().toString())
						.param("title", "Imagen falsa")
						.param("description", "No debe almacenarse")
						.param("price", "10.00"))
				.andExpect(status().isBadRequest());
	}

	private Cookie createAdminAndLogin(String suffix) throws Exception {
		String email = "admin-products-" + suffix + "@flowerdetails.test";
		String password = "Password123";
		User admin = userRepository.save(User.createStaff(
				email,
				passwordHasher.hash(password),
				UserRole.ADMIN
		));
		personRepository.save(Person.create(admin.id(), "Admin", "Productos", null, null));
		return login(email, password);
	}

	private Cookie login(String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(email, password)))
				.andExpect(status().isOk())
				.andReturn();

		Cookie cookie = result.getResponse().getCookie(ACCESS_COOKIE_NAME);
		assertThat(cookie).isNotNull();
		return cookie;
	}

	private MockMultipartFile image(String parameterName, String fileName, byte[] content) {
		String contentType = fileName.endsWith(".webp") ? "image/webp" : "image/png";
		return new MockMultipartFile(parameterName, fileName, contentType, content);
	}

	private Long countProductRowsById(Long id) {
		return jdbcTemplate.queryForObject("select count(*) from products where id = ?", Long.class, id);
	}

	private Long countSoftDeletedProductRowsById(Long id) {
		return jdbcTemplate.queryForObject(
				"select count(*) from products where id = ? and deleted_at is not null",
				Long.class,
				id
		);
	}

	private Long countSoftDeletedImageRowsByProductId(Long productId) {
		return jdbcTemplate.queryForObject(
				"select count(*) from product_images where product_id = ? and deleted_at is not null",
				Long.class,
				productId
		);
	}

	private static Long readLong(MvcResult result, String jsonPath) throws Exception {
		Object value = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), jsonPath);
		if (value instanceof Number number) {
			return number.longValue();
		}
		throw new IllegalStateException("No se pudo leer el valor numerico " + jsonPath);
	}

	private static String readString(MvcResult result, String jsonPath) throws Exception {
		Object value = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), jsonPath);
		if (value instanceof String text) {
			return text;
		}
		throw new IllegalStateException("No se pudo leer el texto " + jsonPath);
	}
}
