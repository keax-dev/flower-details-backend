package com.flower_details.features.users.application.dto.command;

public record UpdateOperatorCommand(
		Long operatorId,
		String names,
		String lastNames,
		String email,
		String phone,
		boolean active
) {
}
