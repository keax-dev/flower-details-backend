package com.flower_details.features.auth.presentation.dto.request;

import com.flower_details.features.auth.application.dto.command.LoginCommand;
import com.flower_details.shared.presentation.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "El correo es obligatorio")
		@Email(message = "El correo no tiene un formato valido")
		String email,

		@NotBlank(message = "La contrasena es obligatoria")
		@StrongPassword(requireStrength = false, message = "La contrasena no puede superar 72 bytes")
		String password
) {

	public LoginCommand toCommand() {
		return new LoginCommand(email, password);
	}
}
