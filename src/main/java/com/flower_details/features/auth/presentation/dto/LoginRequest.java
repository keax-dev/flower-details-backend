package com.flower_details.features.auth.presentation.dto;

import com.flower_details.features.auth.application.dto.LoginCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
		@NotBlank(message = "El correo es obligatorio")
		@Email(message = "El correo no tiene un formato valido")
		String email,

		@NotBlank(message = "La contrasena es obligatoria")
		@Size(max = 72, message = "La contrasena no puede superar 72 caracteres")
		String password
) {

	public LoginCommand toCommand() {
		return new LoginCommand(email, password);
	}
}
