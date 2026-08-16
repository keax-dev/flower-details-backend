package com.flower_details.features.auth.infrastructure.security.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(
		name = "security.authentication-rate-limit.backend",
		havingValue = "memory",
		matchIfMissing = true
)
class InMemoryAuthenticationAttemptRateLimiter implements AuthenticationAttemptRateLimiter {

	private final Map<String, AttemptWindow> attemptsByClient = new ConcurrentHashMap<>();
	private final AuthenticationRateLimitProperties properties;

	InMemoryAuthenticationAttemptRateLimiter(AuthenticationRateLimitProperties properties) {
		this.properties = properties;
	}

	@Override
	public RateLimitDecision tryConsume(String clientKey) {
		Instant now = Instant.now();
		AttemptWindow window = attemptsByClient.compute(clientKey, (key, current) -> {
			if (current == null || !current.expiresAt().isAfter(now)) {
				return new AttemptWindow(1, now.plus(properties.window()));
			}
			return new AttemptWindow(current.attempts() + 1, current.expiresAt());
		});

		if (window.attempts() <= properties.maxAttempts()) {
			return RateLimitDecision.permit();
		}
		return RateLimitDecision.rejected(window.expiresAt().getEpochSecond() - now.getEpochSecond());
	}

	@Override
	public void reset(String clientKey) {
		attemptsByClient.remove(clientKey);
	}

	private record AttemptWindow(int attempts, Instant expiresAt) {
	}
}
