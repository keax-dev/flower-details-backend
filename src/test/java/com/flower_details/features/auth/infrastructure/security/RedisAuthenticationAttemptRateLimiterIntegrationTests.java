package com.flower_details.features.auth.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class RedisAuthenticationAttemptRateLimiterIntegrationTests {

	@Container
	static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4-alpine")
			.withExposedPorts(6379);

	@Autowired
	private AuthenticationAttemptRateLimiter rateLimiter;

	@DynamicPropertySource
	static void configureRedis(DynamicPropertyRegistry registry) {
		registry.add("security.authentication-rate-limit.backend", () -> "redis");
		registry.add("security.authentication-rate-limit.max-attempts", () -> 2);
		registry.add("security.authentication-rate-limit.window-seconds", () -> 60);
		registry.add("spring.data.redis.host", REDIS::getHost);
		registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
	}

	@Test
	void rateLimitIsAtomicAndCanBeResetAfterSuccessfulAuthentication() {
		String clientKey = "203.0.113.10";

		assertThat(rateLimiter.tryConsume(clientKey).allowed()).isTrue();
		assertThat(rateLimiter.tryConsume(clientKey).allowed()).isTrue();
		RateLimitDecision rejected = rateLimiter.tryConsume(clientKey);
		assertThat(rejected.allowed()).isFalse();
		assertThat(rejected.retryAfterSeconds()).isPositive();

		rateLimiter.reset(clientKey);
		assertThat(rateLimiter.tryConsume(clientKey).allowed()).isTrue();
	}
}
