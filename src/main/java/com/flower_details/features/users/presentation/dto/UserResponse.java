package com.flower_details.features.users.presentation.dto;

import com.flower_details.features.users.application.dto.UserProfile;
import com.flower_details.features.users.domain.model.UserRole;

import java.time.Instant;

public record UserResponse(
		Long id,
		Long personId,
		String names,
		String lastNames,
		String email,
		String phone,
		String documentNumber,
		UserRole role,
		boolean active,
		Instant createdAt,
		Instant updatedAt
) {

	public static UserResponse from(UserProfile profile) {
		return new UserResponse(
				profile.id(),
				profile.personId(),
				profile.names(),
				profile.lastNames(),
				profile.email(),
				profile.phone(),
				profile.documentNumber(),
				profile.role(),
				profile.active(),
				profile.createdAt(),
				profile.updatedAt()
		);
	}
}
