package com.flower_details.support;

import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public record CsrfTestToken(Cookie cookie, String headerName, String token) {

	public static CsrfTestToken obtain(MockMvc mockMvc) throws Exception {
		MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
				.andExpect(status().isOk())
				.andReturn();

		Cookie cookie = result.getResponse().getCookie("XSRF-TOKEN");
		assertThat(cookie).isNotNull();
		Object headerName = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.headerName");
		Object token = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.token");
		return new CsrfTestToken(cookie, (String) headerName, (String) token);
	}
}
