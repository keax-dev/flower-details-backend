package com.flower_details.features.users;

import com.flower_details.features.users.application.port.out.UserRepositoryPort;
import com.flower_details.features.users.application.port.out.PersonRepositoryPort;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.shared.security.PasswordHasher;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserSoftDeleteIntegrationTests {

	private static final String ACCESS_COOKIE_NAME = "flower_details_test_access_token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepositoryPort userRepository;

	@Autowired
	private PersonRepositoryPort personRepository;

	@Autowired
	private PasswordHasher passwordHasher;

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
				passwordHasher.hash(password),
				UserRole.ADMIN
		));
		personRepository.save(Person.create(admin.id(), "Admin", "Demo", null, null));

		User operator = userRepository.save(User.createStaff(
				operatorEmail,
				passwordHasher.hash(password),
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

		mockMvc.perform(delete("/api/users/{id}", operator.id()).cookie(adminCookie))
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
