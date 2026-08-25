package com.flower_details.features.category;

import com.flower_details.features.category.domain.repository.CategoryRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryIntegrationTests {

	private static final String ACCESS_COOKIE_NAME = "flower_details_test_access_token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private BCryptPasswordService passwordService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void listCategoriesIsPublicAndReturnsOnlyActiveCategories() throws Exception {
		String suffix = UUID.randomUUID().toString();
		Category active = categoryRepository.save(Category.create(
				"Ramos " + suffix,
				"Arreglos florales para regalos especiales",
				true
		));
		Category inactive = categoryRepository.save(Category.create(
				"Oculta " + suffix,
				"Categoria desactivada para administracion interna",
				false
		));

		MvcResult result = mockMvc.perform(get("/api/categories"))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(result.getResponse().getContentAsString())
				.contains(active.title())
				.doesNotContain(inactive.title());
	}

	@Test
	void administrativeListIncludesInactiveCategoriesAndExcludesSoftDeletedCategories() throws Exception {
		String suffix = UUID.randomUUID().toString();
		Cookie adminCookie = createAdminAndLogin(suffix);
		Category active = categoryRepository.save(Category.create("Activa " + suffix, "Categoria activa", true));
		Category inactive = categoryRepository.save(Category.create("Inactiva " + suffix, "Categoria inactiva", false));
		Category deleted = categoryRepository.save(Category.create("Eliminada " + suffix, "Categoria eliminada", true));
		categoryRepository.delete(deleted);

		MvcResult result = mockMvc.perform(get("/api/categories/administration").cookie(adminCookie))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(result.getResponse().getContentAsString())
				.contains(active.title())
				.contains(inactive.title())
				.doesNotContain(deleted.title());
	}

	@Test
	void adminCanCreateUpdateAndSoftDeleteCategory() throws Exception {
		String suffix = UUID.randomUUID().toString();
		Cookie adminCookie = createAdminAndLogin(suffix);
		CsrfTestToken csrfToken = CsrfTestToken.obtain(mockMvc);

		MvcResult createResult = mockMvc.perform(post("/api/categories")
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Cumpleanos %s",
								  "description": "Detalles florales para cumpleanos",
								  "active": true
								}
								""".formatted(suffix)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Cumpleanos " + suffix))
				.andExpect(jsonPath("$.active").value(true))
				.andReturn();

		Long categoryId = readLong(createResult, "$.id");

		mockMvc.perform(put("/api/categories/{id}", categoryId)
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Aniversarios %s",
								  "description": "Detalles florales para aniversarios",
								  "active": false
								}
								""".formatted(suffix)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Aniversarios " + suffix))
				.andExpect(jsonPath("$.description").value("Detalles florales para aniversarios"))
				.andExpect(jsonPath("$.active").value(false));

		mockMvc.perform(delete("/api/categories/{id}", categoryId)
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token()))
				.andExpect(status().isNoContent());

		assertThat(categoryRepository.findById(categoryId)).isEmpty();
		assertThat(countCategoryRowsById(categoryId)).isEqualTo(1L);
		assertThat(countSoftDeletedCategoryRowsById(categoryId)).isEqualTo(1L);
	}

	@Test
	void anonymousUserCannotCreateCategory() throws Exception {
		mockMvc.perform(post("/api/categories")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Sin permiso",
								  "description": "No deberia crearse",
								  "active": true
								}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deletedCategoryTitleCanBeReused() {
		String title = "Reutilizable " + UUID.randomUUID();
		Category deleted = categoryRepository.save(Category.create(title, "Categoria original", true));
		categoryRepository.delete(deleted);

		Category recreated = categoryRepository.save(Category.create(title, "Categoria recreada", true));

		assertThat(recreated.id()).isNotEqualTo(deleted.id());
	}

	private Cookie createAdminAndLogin(String suffix) throws Exception {
		String email = "admin-catalog-" + suffix + "@flowerdetails.test";
		String password = "Password123";
		User admin = userRepository.save(User.createStaff(
				email,
				passwordService.hash(password),
				UserRole.ADMIN
		));
		personRepository.save(Person.create(admin.id(), "Admin", "Catalogo", null, null));
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

	private Long countCategoryRowsById(Long id) {
		return jdbcTemplate.queryForObject("select count(*) from categories where id = ?", Long.class, id);
	}

	private Long countSoftDeletedCategoryRowsById(Long id) {
		return jdbcTemplate.queryForObject(
				"select count(*) from categories where id = ? and deleted_at is not null",
				Long.class,
				id
		);
	}

	private static Long readLong(MvcResult result, String jsonPath) throws Exception {
		Object value = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), jsonPath);
		if (value instanceof Number number) {
			return number.longValue();
		}
		throw new IllegalStateException("No se pudo leer el valor numerico " + jsonPath);
	}
}
