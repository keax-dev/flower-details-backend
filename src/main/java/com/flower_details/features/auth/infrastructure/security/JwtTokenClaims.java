package com.flower_details.features.auth.infrastructure.security;

import com.flower_details.features.users.domain.model.UserRole;

record JwtTokenClaims(
		Long userId,
		String email,
		UserRole role
) {
}
