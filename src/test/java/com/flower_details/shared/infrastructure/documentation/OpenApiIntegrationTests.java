package com.flower_details.shared.infrastructure.documentation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void publishesOpenApiDocumentationWithoutAuthenticationOutsideProduction() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("Flower Details API"))
				.andExpect(jsonPath("$.paths['/api/orders']").exists())
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"));
	}
}
