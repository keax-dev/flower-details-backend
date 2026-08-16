package com.flower_details.features.auth.infrastructure.security.ratelimit;

class AuthenticationRateLimitUnavailableException extends RuntimeException {

	AuthenticationRateLimitUnavailableException() {
		super("El servicio de rate limit no esta disponible");
	}

	AuthenticationRateLimitUnavailableException(Throwable cause) {
		super("El servicio de rate limit no esta disponible", cause);
	}
}
