package com.flower_details.features.auth.infrastructure.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@RequiredArgsConstructor
public class AuthenticationRateLimitFilter extends OncePerRequestFilter {

	private final AuthenticationAttemptRateLimiter rateLimiter;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !HttpMethod.POST.matches(request.getMethod())
				|| !("/api/auth/login".equals(request.getRequestURI()) || "/api/auth/register".equals(request.getRequestURI()));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String clientKey = clientKey(request);
		RateLimitDecision decision;
		try {
			decision = rateLimiter.tryConsume(clientKey);
		}
		catch (AuthenticationRateLimitUnavailableException exception) {
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.getWriter().write("{\"message\":\"Servicio de autenticacion temporalmente no disponible\"}");
			return;
		}

		if (!decision.allowed()) {
			response.setStatus(429);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(decision.retryAfterSeconds()));
			response.getWriter().write("{\"message\":\"Demasiados intentos. Intenta nuevamente mas tarde\"}");
			return;
		}

		filterChain.doFilter(request, response);
		if (response.getStatus() >= 200 && response.getStatus() < 300) {
			try {
				rateLimiter.reset(clientKey);
			}
			catch (AuthenticationRateLimitUnavailableException ignored) {
				// The successful response must not be converted into an authentication failure.
			}
		}
	}

	private static String clientKey(HttpServletRequest request) {
		return request.getRemoteAddr();
	}

}
