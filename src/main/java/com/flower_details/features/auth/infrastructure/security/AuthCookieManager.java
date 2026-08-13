package com.flower_details.features.auth.infrastructure.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthCookieManager {

	private final AuthCookieProperties properties;

	public ResponseCookie createAccessTokenCookie(String token, long expiresInSeconds) {
		ResponseCookie.ResponseCookieBuilder builder = baseCookie(token)
				.maxAge(Duration.ofSeconds(expiresInSeconds));

		return builder.build();
	}

	public ResponseCookie clearAccessTokenCookie() {
		return baseCookie("")
				.maxAge(Duration.ZERO)
				.build();
	}

	public Optional<String> resolveAccessToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null || cookies.length == 0) {
			return Optional.empty();
		}

		return Arrays.stream(cookies)
				.filter(cookie -> properties.accessTokenName().equals(cookie.getName()))
				.map(Cookie::getValue)
				.filter(value -> value != null && !value.isBlank())
				.findFirst();
	}

	public boolean hasAccessToken(HttpServletRequest request) {
		return resolveAccessToken(request).isPresent();
	}

	private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
		ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.accessTokenName(), value)
				.httpOnly(true)
				.secure(properties.secure())
				.path(properties.path())
				.sameSite(properties.sameSite());

		if (properties.hasDomain()) {
			builder.domain(properties.domain());
		}

		return builder;
	}
}
