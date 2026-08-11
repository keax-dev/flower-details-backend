package com.flower_details.features.auth.application.dto;

public record LoginCommand(
		String email,
		String password
) {
}
