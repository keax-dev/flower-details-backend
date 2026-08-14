package com.flower_details.features.users.application.dto.command;

public record CreateOperatorCommand(
		String names,
		String lastNames,
		String email,
		String password,
		String phone,
		String documentNumber
) {
}
