package com.flower_details.features.auth.infrastructure.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "security.authentication-rate-limit.backend", havingValue = "redis")
class RedisAuthenticationAttemptRateLimiter implements AuthenticationAttemptRateLimiter {

	private static final DefaultRedisScript<List> INCREMENT_ATTEMPT_SCRIPT = new DefaultRedisScript<>(
				"""
						local attempts = redis.call('INCR', KEYS[1])
						if attempts == 1 then
						  redis.call('EXPIRE', KEYS[1], ARGV[1])
						end
						return {attempts, redis.call('TTL', KEYS[1])}
						""",
				List.class
		);

	private final StringRedisTemplate redisTemplate;
	private final AuthenticationRateLimitProperties properties;

	RedisAuthenticationAttemptRateLimiter(
			StringRedisTemplate redisTemplate,
			AuthenticationRateLimitProperties properties
	) {
		this.redisTemplate = redisTemplate;
		this.properties = properties;
	}

	@Override
	public RateLimitDecision tryConsume(String clientKey) {
		try {
			List<?> result = redisTemplate.execute(
					INCREMENT_ATTEMPT_SCRIPT,
					List.of(redisKey(clientKey)),
					Long.toString(properties.windowSeconds())
			);
			if (result == null || result.size() != 2) {
				throw new AuthenticationRateLimitUnavailableException();
			}

			long attempts = ((Number) result.get(0)).longValue();
			long ttl = ((Number) result.get(1)).longValue();
			return attempts <= properties.maxAttempts()
					? RateLimitDecision.permit()
					: RateLimitDecision.rejected(ttl);
		}
		catch (DataAccessException | ClassCastException exception) {
			throw new AuthenticationRateLimitUnavailableException(exception);
		}
	}

	@Override
	public void reset(String clientKey) {
		try {
			redisTemplate.delete(redisKey(clientKey));
		}
		catch (DataAccessException exception) {
			throw new AuthenticationRateLimitUnavailableException(exception);
		}
	}

	private String redisKey(String clientKey) {
		return properties.redisKeyPrefix() + clientKey;
	}
}
