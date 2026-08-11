package com.flower_details.features.auth.presentation.dto;

import com.flower_details.features.auth.application.dto.AuthResult;

public record AuthResponse(
		long expiresInSeconds,
		AuthUserResponse user
) {

	public static AuthResponse from(AuthResult result) {
		return new AuthResponse(
				result.expiresInSeconds(),
				AuthUserResponse.from(result.user())
		);
	}
}
