package com.flower_details.features.auth.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
record AuthenticationRateLimitProperties(
		@Value("${security.authentication-rate-limit.max-attempts:10}") int maxAttempts,
		@Value("${security.authentication-rate-limit.window-seconds:60}") long windowSeconds,
		@Value("${security.authentication-rate-limit.redis-key-prefix:flower-details:auth-rate-limit:}") String redisKeyPrefix
) {

	AuthenticationRateLimitProperties {
		if (maxAttempts < 1 || windowSeconds < 1) {
			throw new IllegalArgumentException("La configuracion de rate limit debe ser positiva");
		}
		if (redisKeyPrefix == null || redisKeyPrefix.isBlank()) {
			throw new IllegalArgumentException("El prefijo Redis de rate limit es obligatorio");
		}
	}

	Duration window() {
		return Duration.ofSeconds(windowSeconds);
	}
}
