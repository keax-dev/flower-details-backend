package com.flower_details.features.auth.infrastructure.security.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
class ClientIpResolver {

	private final TrustedProxyProperties trustedProxyProperties;

	String resolve(HttpServletRequest request) {
		String remoteAddress = request.getRemoteAddr();
		if (!trustedProxyProperties.isTrusted(remoteAddress)) {
			return remoteAddress;
		}

		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor == null || forwardedFor.isBlank()) {
			return remoteAddress;
		}

		List<String> addresses = Arrays.stream(forwardedFor.split(","))
				.map(String::trim)
				.toList();
		for (int index = addresses.size() - 1; index >= 0; index--) {
			String candidate = addresses.get(index);
			if (trustedProxyProperties.isIpAddress(candidate) && !trustedProxyProperties.isTrusted(candidate)) {
				return candidate;
			}
		}

		return remoteAddress;
	}
}
