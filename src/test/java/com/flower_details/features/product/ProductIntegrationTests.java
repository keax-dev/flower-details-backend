package com.flower_details.features.product;

import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.repository.ProductRepository;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.users.domain.repository.PersonRepository;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.shared.infrastructure.security.BCryptPasswordService;
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

import java.util.UUID;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductIntegrationTests {

	private static final String ACCESS_COOKIE_NAME = "flower_details_test_access_token";
	private static final byte[] FIRST_IMAGE = pngImage();
	private static final byte[] SECOND_IMAGE = FIRST_IMAGE;

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

		MvcResult createResult = mockMvc.perform(post("/api/products")
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "categoryId": %d,
								  "title": "Ramo premium %s",
								  "description": "Ramo floral premium",
								  "price": 29.90,
								  "active": true
								}
								""".formatted(category.id(), suffix)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Ramo premium " + suffix))
				.andExpect(jsonPath("$.category.id").value(category.id()))
				.andExpect(jsonPath("$.images").isEmpty())
				.andReturn();

		Long productId = readLong(createResult, "$.id");
		MvcResult firstUploadResult = mockMvc.perform(multipart("/api/products/{id}/images", productId)
						.file(image("images", "ramo.png", FIRST_IMAGE))
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.images[0].contentType").value("image/png"))
				.andReturn();

		String firstImageUrl = readString(firstUploadResult, "$.images[0].url");

		mockMvc.perform(get("/api/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].id").value(productId));

		mockMvc.perform(get("/api/products/{id}", productId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.price").value(29.90));

		mockMvc.perform(get(firstImageUrl))
				.andExpect(status().isOk())
				.andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(FIRST_IMAGE));

		mockMvc.perform(put("/api/products/{id}", productId)
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "categoryId": %d,
								  "title": "Ramo actualizado %s",
								  "description": "Ramo floral actualizado",
								  "price": 35.50,
								  "active": true
								}
								""".formatted(category.id(), suffix)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Ramo actualizado " + suffix));

		MvcResult secondUploadResult = mockMvc.perform(multipart("/api/products/{id}/images", productId)
						.file(image("images", "actualizado.png", SECOND_IMAGE))
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.images.length()").value(2))
				.andReturn();

		String secondImageUrl = readString(secondUploadResult, "$.images[1].url");

		mockMvc.perform(get(firstImageUrl))
				.andExpect(status().isOk());
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
		mockMvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"categoryId":1,"title":"Sin permiso","description":"No deberia crearse","price":10.00,"active":true}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void publicCatalogFiltersActiveProductsAndAdminCanSearchInactiveProducts() throws Exception {
		String suffix = UUID.randomUUID().toString();
		Cookie adminCookie = createAdminAndLogin(suffix);
		Category category = categoryRepository.save(Category.create("Filtros " + suffix, "Categoria de filtros", true));
		Product visibleProduct = productRepository.save(Product.create(
				category.id(), "Rosa roja " + suffix, "Ramo de rosas", new java.math.BigDecimal("25.00"), true
		));
		Product hiddenProduct = productRepository.save(Product.create(
				category.id(), "Rosa oculta " + suffix, "Producto inactivo", new java.math.BigDecimal("10.00"), false
		));

		mockMvc.perform(get("/api/products")
						.param("q", "Rosa roja " + suffix)
						.param("categoryId", category.id().toString())
						.param("minPrice", "20")
						.param("maxPrice", "30")
						.param("sortBy", "price")
						.param("direction", "asc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].id").value(visibleProduct.id()));

		mockMvc.perform(get("/api/products").param("q", "Rosa oculta " + suffix))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isEmpty());

		mockMvc.perform(get("/api/products/manage")
						.cookie(adminCookie)
						.param("q", "Rosa oculta " + suffix)
						.param("active", "false"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].id").value(hiddenProduct.id()));
	}

	@Test
	void cookieAuthenticatedUserNeedsCsrfTokenToCreateProduct() throws Exception {
		String suffix = UUID.randomUUID().toString();
		Cookie adminCookie = createAdminAndLogin(suffix);
		Category category = categoryRepository.save(Category.create("CSRF " + suffix, "Categoria de prueba", true));

		mockMvc.perform(post("/api/products")
						.cookie(adminCookie)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"categoryId":%d,"title":"Sin CSRF","description":"No debe crearse","price":10.00,"active":true}
								""".formatted(category.id())))
				.andExpect(status().isForbidden());
	}

	@Test
	void rejectsFileWhoseContentDoesNotMatchDeclaredImageType() throws Exception {
		String suffix = UUID.randomUUID().toString();
		Cookie adminCookie = createAdminAndLogin(suffix);
		CsrfTestToken csrfToken = CsrfTestToken.obtain(mockMvc);
		Category category = categoryRepository.save(Category.create("Imagen invalida " + suffix, "Categoria de prueba", true));
		var product = productRepository.save(com.flower_details.features.product.domain.model.Product.create(
				category.id(), "Producto invalido", "Producto para validar archivo", new java.math.BigDecimal("10.00"), true
		));

		mockMvc.perform(multipart("/api/products/{id}/images", product.id())
						.file(new MockMultipartFile("images", "falso.png", "image/png", new byte[] {1, 2, 3, 4}))
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token()))
				.andExpect(status().isBadRequest());
	}

	private Cookie createAdminAndLogin(String suffix) throws Exception {
		String email = "admin-products-" + suffix + "@flowerdetails.test";
		String password = "Password123";
		User admin = userRepository.save(User.createStaff(
				email,
				passwordService.hash(password),
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
		return new MockMultipartFile(parameterName, fileName, "image/png", content);
	}

	private static byte[] pngImage() {
		try {
			BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			image.setRGB(0, 0, 0xFFFF66AA);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write(image, "png", output);
			return output.toByteArray();
		}
		catch (IOException exception) {
			throw new IllegalStateException("No se pudo crear la imagen de prueba", exception);
		}
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
