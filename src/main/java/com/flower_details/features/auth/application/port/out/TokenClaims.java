package com.flower_details.features.auth.application.port.out;

import com.flower_details.features.users.domain.model.UserRole;

public record TokenClaims(
		Long userId,
		String email,
		UserRole role
) {
}
