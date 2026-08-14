package com.flower_details.features.auth.infrastructure.security;

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
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
class AuthenticationRateLimitFilter extends OncePerRequestFilter {

	private final Map<String, AttemptWindow> attemptsByClient = new ConcurrentHashMap<>();

	private final AuthenticationRateLimitProperties properties;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !HttpMethod.POST.matches(request.getMethod())
				|| !("/api/auth/login".equals(request.getRequestURI()) || "/api/auth/register".equals(request.getRequestURI()));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String clientKey = clientKey(request);
		Instant now = Instant.now();
		AttemptWindow window = attemptsByClient.get(clientKey);
		if (window != null && window.expiresAt().isAfter(now) && window.attempts() >= properties.maxAttempts()) {
			long retryAfterSeconds = Math.max(1, window.expiresAt().getEpochSecond() - now.getEpochSecond());
			response.setStatus(429);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(retryAfterSeconds));
			response.getWriter().write("{\"message\":\"Demasiados intentos. Intenta nuevamente mas tarde\"}");
			return;
		}

		filterChain.doFilter(request, response);
		if (response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED) {
			recordFailedAttempt(clientKey, now);
		}
		else if (response.getStatus() >= 200 && response.getStatus() < 300) {
			attemptsByClient.remove(clientKey);
		}
	}

	private void recordFailedAttempt(String clientKey, Instant now) {
		attemptsByClient.compute(clientKey, (key, current) -> {
			if (current == null || !current.expiresAt().isAfter(now)) {
				return new AttemptWindow(1, now.plus(properties.window()));
			}
			return new AttemptWindow(current.attempts() + 1, current.expiresAt());
		});
	}

	private static String clientKey(HttpServletRequest request) {
		return request.getRemoteAddr();
	}

	private record AttemptWindow(int attempts, Instant expiresAt) {
	}
}
