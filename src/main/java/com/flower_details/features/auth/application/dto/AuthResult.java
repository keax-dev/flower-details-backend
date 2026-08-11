package com.flower_details.features.auth.application.dto;

import com.flower_details.features.users.application.dto.UserProfile;

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
