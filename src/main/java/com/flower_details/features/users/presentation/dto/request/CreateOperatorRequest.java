package com.flower_details.features.users.presentation.dto.request;

import com.flower_details.features.users.application.dto.command.CreateOperatorCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOperatorRequest(
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
		@Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
		String password,

		@Size(max = 30, message = "El telefono no puede superar 30 caracteres")
		String phone,

		@Size(max = 30, message = "El documento no puede superar 30 caracteres")
		String documentNumber
) {

	public CreateOperatorCommand toCommand() {
		return new CreateOperatorCommand(names, lastNames, email, password, phone, documentNumber);
	}
}
