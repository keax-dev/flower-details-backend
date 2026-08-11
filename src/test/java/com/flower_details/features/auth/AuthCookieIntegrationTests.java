package com.flower_details.features.auth;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthCookieIntegrationTests {

	private static final String ACCESS_COOKIE_NAME = "flower_details_test_access_token";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void registerSetsHttpOnlyCookieWithoutExposingJwtInBody() throws Exception {
		String email = "cliente-" + UUID.randomUUID() + "@flowerdetails.test";
		String documentNumber = "DOC" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "names": "Cliente",
								  "lastNames": "Demo",
								  "email": "%s",
								  "password": "Password123",
								  "phone": "0999999999",
								  "documentNumber": "%s"
								}
								""".formatted(email, documentNumber)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.user.email").value(email))
				.andExpect(jsonPath("$.user.names").value("Cliente"))
				.andExpect(jsonPath("$.user.lastNames").value("Demo"))
				.andReturn();

		String setCookie = registerResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);
		String responseBody = registerResult.getResponse().getContentAsString();
		Cookie accessCookie = registerResult.getResponse().getCookie(ACCESS_COOKIE_NAME);

		assertThat(setCookie)
				.contains(ACCESS_COOKIE_NAME + "=")
				.contains("HttpOnly")
				.contains("SameSite=Lax")
				.doesNotContain("Secure");
		assertThat(responseBody).doesNotContain("accessToken");
		assertThat(accessCookie).isNotNull();

		mockMvc.perform(get("/api/me").cookie(accessCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.names").value("Cliente"));
	}

	@Test
	void logoutClearsAccessTokenCookie() throws Exception {
		MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout"))
				.andExpect(status().isNoContent())
				.andReturn();

		assertThat(logoutResult.getResponse().getHeader(HttpHeaders.SET_COOKIE))
				.contains(ACCESS_COOKIE_NAME + "=")
				.contains("Max-Age=0")
				.contains("HttpOnly");
	}
}
