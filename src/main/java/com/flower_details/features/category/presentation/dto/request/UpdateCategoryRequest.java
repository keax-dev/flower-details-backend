package com.flower_details.features.category.presentation.dto.request;

import com.flower_details.features.category.application.dto.command.UpdateCategoryCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
		@NotBlank(message = "El titulo es obligatorio")
		@Size(max = 120, message = "El titulo no puede superar 120 caracteres")
		String title,

		@NotBlank(message = "La descripcion es obligatoria")
		@Size(max = 20_000, message = "La descripcion supera el limite permitido")
		String description,

		@NotNull(message = "El estado activo es obligatorio")
		Boolean active
) {

	public UpdateCategoryCommand toCommand(Long id) {
		return new UpdateCategoryCommand(id, title, description, active);
	}
}
