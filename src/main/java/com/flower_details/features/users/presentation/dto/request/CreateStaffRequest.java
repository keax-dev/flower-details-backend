package com.flower_details.features.users.presentation.dto.request;

import com.flower_details.features.users.application.dto.command.CreateStaffCommand;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.shared.presentation.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStaffRequest(
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

		@NotBlank(message = "El telefono es obligatorio")
		@Size(max = 30, message = "El telefono no puede superar 30 caracteres")
		String phone,

		@NotNull(message = "El rol es obligatorio")
		UserRole role,

		@NotNull(message = "El estado del usuario es obligatorio")
		Boolean active
) {

	public CreateStaffCommand toCommand() {
		return new CreateStaffCommand(names, lastNames, email, password, phone, role, active);
	}
}
