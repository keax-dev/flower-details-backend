package com.flower_details.features.auth.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
record CorsProperties(
		@Value("${security.cors.allowed-origins:http://localhost:4200}") String allowedOrigins
) {

	List<String> origins() {
		return Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isBlank())
				.toList();
	}
}
