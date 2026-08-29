package com.flower_details.features.users;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserSoftDeleteIntegrationTests {

	private static final String ACCESS_COOKIE_NAME = "flower_details_test_access_token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private BCryptPasswordService passwordService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void deleteUserMarksDeletedAtWithoutRemovingTheRow() throws Exception {
		String suffix = UUID.randomUUID().toString();
		String adminEmail = "admin-" + suffix + "@flowerdetails.test";
		String operatorEmail = "operator-" + suffix + "@flowerdetails.test";
		String password = "Password123";

		User admin = userRepository.save(User.createStaff(
				adminEmail,
				passwordService.hash(password),
				UserRole.ADMIN
		));
		personRepository.save(Person.create(admin.id(), "Admin", "Demo", null, null));

		User operator = userRepository.save(User.createStaff(
				operatorEmail,
				passwordService.hash(password),
				UserRole.OPERATOR
		));
		personRepository.save(Person.create(
				operator.id(),
				"Operator",
				"Demo",
				null,
				"DOC" + suffix.replace("-", "").substring(0, 12)
		));

		Cookie adminCookie = login(admin.email(), password);
		CsrfTestToken csrfToken = CsrfTestToken.obtain(mockMvc);

		mockMvc.perform(delete("/api/users/{id}", operator.id())
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token()))
				.andExpect(status().isNoContent());

		assertThat(userRepository.findById(operator.id())).isEmpty();
		assertThat(personRepository.findByUserId(operator.id())).isEmpty();
		assertThat(countUserRowsById(operator.id())).isEqualTo(1L);
		assertThat(countSoftDeletedUserRowsById(operator.id())).isEqualTo(1L);
		assertThat(countPeopleRowsByUserId(operator.id())).isEqualTo(1L);
		assertThat(countSoftDeletedPeopleRowsByUserId(operator.id())).isEqualTo(1L);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(operatorEmail, password)))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/users/operators")
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "names": "Nuevo operador",
								  "lastNames": "Demo",
								  "email": "%s",
								  "password": "%s",
								  "phone": "0988888888",
								  "active": true
								}
								""".formatted(operatorEmail, password)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value(operatorEmail));

	}

	@Test
	void adminCanCreateListDeactivateAndReactivateAnOperator() throws Exception {
		String suffix = UUID.randomUUID().toString();
		String adminEmail = "admin-" + suffix + "@flowerdetails.test";
		String operatorEmail = "operator-" + suffix + "@flowerdetails.test";
		String password = "Password123";

		User admin = userRepository.save(User.createStaff(
				adminEmail,
				passwordService.hash(password),
				UserRole.ADMIN
		));
		personRepository.save(Person.create(admin.id(), "Admin", "Demo", null, null));

		Cookie adminCookie = login(admin.email(), password);
		CsrfTestToken csrfToken = CsrfTestToken.obtain(mockMvc);

		MvcResult createResult = mockMvc.perform(post("/api/users/operators")
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "names": "Operator",
								  "lastNames": "Demo",
								  "email": "%s",
								  "password": "%s",
								  "phone": "0999999999",
								  "active": true
								}
								""".formatted(operatorEmail, password)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.role").value("OPERATOR"))
				.andExpect(jsonPath("$.active").value(true))
				.andExpect(jsonPath("$.passwordHash").doesNotExist())
				.andReturn();
		Long operatorId = ((Number) com.jayway.jsonpath.JsonPath.read(
				createResult.getResponse().getContentAsString(), "$.id"
		)).longValue();

		mockMvc.perform(get("/api/users/operators").param("size", "1").cookie(adminCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.id == %s)].email".formatted(operatorId)).value(operatorEmail));

		mockMvc.perform(put("/api/users/operators/{id}", operatorId)
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "names": "Operator Updated",
								  "lastNames": "Demo Updated",
								  "email": "%s",
								  "phone": "0988888888",
								  "active": false
								}
								""".formatted(operatorEmail)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.names").value("Operator Updated"))
				.andExpect(jsonPath("$.active").value(false));

		mockMvc.perform(patch("/api/users/{id}/deactivate", operatorId)
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.active").value(false));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"%s"}
								""".formatted(operatorEmail, password)))
				.andExpect(status().isForbidden());

		mockMvc.perform(patch("/api/users/{id}/activate", operatorId)
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.active").value(true));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"%s"}
								""".formatted(operatorEmail, password)))
				.andExpect(status().isOk());
	}

	@Test
	void adminCannotDeactivateTheirOwnAccount() throws Exception {
		String suffix = UUID.randomUUID().toString();
		String password = "Password123";
		User admin = userRepository.save(User.createStaff(
				"admin-" + suffix + "@flowerdetails.test",
				passwordService.hash(password),
				UserRole.ADMIN
		));
		personRepository.save(Person.create(admin.id(), "Admin", "Demo", null, null));

		Cookie adminCookie = login(admin.email(), password);
		CsrfTestToken csrfToken = CsrfTestToken.obtain(mockMvc);

		mockMvc.perform(patch("/api/users/{id}/deactivate", admin.id())
						.cookie(adminCookie, csrfToken.cookie())
						.header(csrfToken.headerName(), csrfToken.token()))
				.andExpect(status().isBadRequest());
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

	private Long countUserRowsById(Long id) {
		return jdbcTemplate.queryForObject("select count(*) from users where id = ?", Long.class, id);
	}

	private Long countSoftDeletedUserRowsById(Long id) {
		return jdbcTemplate.queryForObject(
				"select count(*) from users where id = ? and deleted_at is not null",
				Long.class,
				id
		);
	}

	private Long countPeopleRowsByUserId(Long userId) {
		return jdbcTemplate.queryForObject("select count(*) from people where user_id = ?", Long.class, userId);
	}

	private Long countSoftDeletedPeopleRowsByUserId(Long userId) {
		return jdbcTemplate.queryForObject(
				"select count(*) from people where user_id = ? and deleted_at is not null",
				Long.class,
				userId
		);
	}
}
