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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
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
	void csrfEndpointReturnsTheSameTokenStoredInCookieForSpaClients() throws Exception {
		MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
				.andReturn();

		Cookie csrfCookie = result.getResponse().getCookie("XSRF-TOKEN");
		String responseToken = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.token");

		assertThat(csrfCookie).isNotNull();
		assertThat(responseToken).isEqualTo(csrfCookie.getValue());
	}

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
	void registerRejectsAWeakPassword() throws Exception {
		mockMvc.perform(post("/api/auth/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "names": "Cliente",
							  "lastNames": "Demo",
							  "email": "weak-%s@flowerdetails.test",
							  "password": "solominsculas123"
							}
							""".formatted(UUID.randomUUID())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors.password").exists());
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

	@Test
	void corsAllowsPatchRequestsForOrderWorkflow() throws Exception {
		mockMvc.perform(options("/api/orders/1/status")
						.header(HttpHeaders.ORIGIN, "http://localhost:4200")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH"))
				.andExpect(status().isOk())
				.andExpect(result -> assertThat(result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
						.contains("PATCH"));
	}

	@Test
	void stateChangingRequestsWithAccessCookieStillNeedCsrfToken() throws Exception {
		Cookie fakeAccessTokenCookie = new Cookie(ACCESS_COOKIE_NAME, "fake-token");

		mockMvc.perform(delete("/api/categories/1")
					.cookie(fakeAccessTokenCookie))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("CSRF_TOKEN_INVALID"));
	}
}
