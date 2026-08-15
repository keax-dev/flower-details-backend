package com.flower_details.features.auth.infrastructure.security;

interface AuthenticationAttemptRateLimiter {

	RateLimitDecision tryConsume(String clientKey);

	void reset(String clientKey);
}
