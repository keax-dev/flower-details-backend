package com.flower_details.features.auth.infrastructure.security.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public record AuthenticationRateLimitProperties(
		@Value("${security.authentication-rate-limit.max-attempts:10}") int maxAttempts,
		@Value("${security.authentication-rate-limit.window-seconds:60}") long windowSeconds,
		@Value("${security.authentication-rate-limit.backend:memory}") String backend,
		@Value("${security.authentication-rate-limit.redis-key-prefix:flower-details:auth-rate-limit:}") String redisKeyPrefix
) {

	public AuthenticationRateLimitProperties {
		if (maxAttempts < 1 || windowSeconds < 1) {
			throw new IllegalArgumentException("La configuracion de rate limit debe ser positiva");
		}
		if (!"memory".equals(backend) && !"redis".equals(backend)) {
			throw new IllegalArgumentException("El backend de rate limit debe ser memory o redis");
		}
		if (redisKeyPrefix == null || redisKeyPrefix.isBlank()) {
			throw new IllegalArgumentException("El prefijo Redis de rate limit es obligatorio");
		}
	}

	Duration window() {
		return Duration.ofSeconds(windowSeconds);
	}
}
