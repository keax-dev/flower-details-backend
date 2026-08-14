package com.flower_details.features.auth.application.dto.command;

public record LoginCommand(
		String email,
		String password
) {
}
