package com.flower_details.features.auth.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
record AuthCookieProperties(
		@Value("${security.cookie.access-token-name:flower_details_access_token}")
		String accessTokenName,

		@Value("${security.cookie.secure:false}")
		boolean secure,

		@Value("${security.cookie.same-site:Lax}")
		String sameSite,

		@Value("${security.cookie.path:/}")
		String path,

		@Value("${security.cookie.domain:}")
		String domain
) {

	AuthCookieProperties {
		if (accessTokenName == null || accessTokenName.isBlank()) {
			accessTokenName = "flower_details_access_token";
		}
		if (sameSite == null || sameSite.isBlank()) {
			sameSite = "Lax";
		}
		if (path == null || path.isBlank()) {
			path = "/";
		}
		domain = normalizeOptional(domain);
	}

	boolean hasDomain() {
		return domain != null;
	}

	private static String normalizeOptional(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
