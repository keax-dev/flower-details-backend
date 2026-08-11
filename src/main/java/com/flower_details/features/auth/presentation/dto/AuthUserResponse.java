package com.flower_details.features.auth.presentation.dto;

import com.flower_details.features.users.application.dto.UserProfile;
import com.flower_details.features.users.domain.model.UserRole;

public record AuthUserResponse(
		Long id,
		Long personId,
		String names,
		String lastNames,
		String email,
		String phone,
		String documentNumber,
		UserRole role
) {

	public static AuthUserResponse from(UserProfile profile) {
		return new AuthUserResponse(
				profile.id(),
				profile.personId(),
				profile.names(),
				profile.lastNames(),
				profile.email(),
				profile.phone(),
				profile.documentNumber(),
				profile.role()
		);
	}
}
