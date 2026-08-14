package com.flower_details.features.auth.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public record JwtProperties(
		@Value("${security.jwt.secret:flower-details-local-development-secret-change-me-please-32bytes}")
		String secret,

		@Value("${security.jwt.expiration-minutes:1440}")
		long expirationMinutes,

		@Value("${security.jwt.issuer:flower-details-api}")
		String issuer,

		@Value("${security.jwt.audience:flower-details-web}")
		String audience
) {

	public JwtProperties {
		if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalArgumentException("security.jwt.secret debe tener al menos 32 bytes");
		}
		if (expirationMinutes <= 0) {
			throw new IllegalArgumentException("security.jwt.expiration-minutes debe ser mayor a cero");
		}
		if (issuer == null || issuer.isBlank() || audience == null || audience.isBlank()) {
			throw new IllegalArgumentException("security.jwt.issuer y security.jwt.audience son obligatorios");
		}
	}

	public Duration expiration() {
		return Duration.ofMinutes(expirationMinutes);
	}
}
