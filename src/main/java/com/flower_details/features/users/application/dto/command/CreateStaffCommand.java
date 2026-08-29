package com.flower_details.features.users.application.dto.command;

import com.flower_details.features.users.domain.model.UserRole;

public record CreateStaffCommand(
		String names,
		String lastNames,
		String email,
		String password,
		String phone,
		UserRole role,
		boolean active
) {
}
