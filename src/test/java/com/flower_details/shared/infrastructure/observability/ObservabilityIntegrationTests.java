package com.flower_details.shared.infrastructure.observability;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ObservabilityIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthEndpointIsPublicAndDoesNotExposeComponentDetails() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist());
	}

	@Test
	void propagatesValidRequestIdToTheResponse() throws Exception {
		mockMvc.perform(get("/api/categories").header("X-Request-Id", "catalog-request-123"))
				.andExpect(status().isOk())
				.andExpect(header().string("X-Request-Id", "catalog-request-123"));
	}

	@Test
	void generatesRequestIdWhenIncomingHeaderIsInvalid() throws Exception {
		mockMvc.perform(get("/api/categories").header("X-Request-Id", "invalid request id!"))
				.andExpect(status().isOk())
				.andExpect(header().exists("X-Request-Id"));
	}
}
