package com.flower_details.features.auth.presentation.dto.request;

import com.flower_details.features.auth.application.dto.command.RegisterCustomerCommand;
import com.flower_details.shared.presentation.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCustomerRequest(
		@NotBlank(message = "Los nombres son obligatorios")
		@Size(max = 80, message = "Los nombres no pueden superar 80 caracteres")
		String names,

		@NotBlank(message = "Los apellidos son obligatorios")
		@Size(max = 80, message = "Los apellidos no pueden superar 80 caracteres")
		String lastNames,

		@NotBlank(message = "El correo es obligatorio")
		@Email(message = "El correo no tiene un formato valido")
		@Size(max = 160, message = "El correo no puede superar 160 caracteres")
		String email,

		@NotBlank(message = "La contrasena es obligatoria")
		@StrongPassword
		String password,

		@Size(max = 30, message = "El telefono no puede superar 30 caracteres")
		String phone,

		@Size(max = 30, message = "El documento no puede superar 30 caracteres")
		String documentNumber
) {

	public RegisterCustomerCommand toCommand() {
		return new RegisterCustomerCommand(names, lastNames, email, password, phone, documentNumber);
	}
}
