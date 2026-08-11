package com.flower_details.features.users.application.dto;

import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;

import java.time.Instant;

public record UserProfile(
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

	public static UserProfile from(User user, Person person) {
		return new UserProfile(
				user.id(),
				person.id(),
				person.names(),
				person.lastNames(),
				user.email(),
				person.phone(),
				person.documentNumber(),
				user.role(),
				user.active(),
				user.createdAt(),
				user.updatedAt()
		);
	}
}
