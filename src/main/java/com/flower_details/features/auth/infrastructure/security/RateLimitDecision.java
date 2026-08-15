package com.flower_details.features.auth.infrastructure.security;

record RateLimitDecision(boolean allowed, long retryAfterSeconds) {

	static RateLimitDecision permit() {
		return new RateLimitDecision(true, 0);
	}

	static RateLimitDecision rejected(long retryAfterSeconds) {
		return new RateLimitDecision(false, Math.max(1, retryAfterSeconds));
	}
}
