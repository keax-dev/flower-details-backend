package com.flower_details.features.auth.application.dto.view;

import com.flower_details.features.users.application.dto.view.UserProfile;

public record AuthResult(
		String accessToken,
		String tokenType,
		long expiresInSeconds,
		UserProfile user
) {

	public static AuthResult bearer(String accessToken, long expiresInSeconds, UserProfile user) {
		return new AuthResult(accessToken, "Bearer", expiresInSeconds, user);
	}
}
