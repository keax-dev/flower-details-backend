package com.flower_details.features.auth.application.dto;

public record RegisterCustomerCommand(
		String names,
		String lastNames,
		String email,
		String password,
		String phone,
		String documentNumber
) {
}
