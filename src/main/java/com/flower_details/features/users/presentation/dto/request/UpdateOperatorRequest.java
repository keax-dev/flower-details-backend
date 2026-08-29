package com.flower_details.features.users.presentation.dto.request;

import com.flower_details.features.users.application.dto.command.UpdateOperatorCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateOperatorRequest(
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

		@NotBlank(message = "El telefono es obligatorio")
		@Size(max = 30, message = "El telefono no puede superar 30 caracteres")
		String phone,

		@NotNull(message = "El estado del operador es obligatorio")
		Boolean active
) {

	public UpdateOperatorCommand toCommand(Long operatorId) {
		return new UpdateOperatorCommand(operatorId, names, lastNames, email, phone, active);
	}
}
