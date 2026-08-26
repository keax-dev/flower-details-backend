package com.flower_details.features.product.presentation.dto.request;

import com.flower_details.features.product.application.dto.command.UpdateProductCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
		@NotNull(message = "La categoria es obligatoria")
		Long categoryId,

		@NotBlank(message = "El titulo es obligatorio")
		@Size(max = 160, message = "El titulo no puede superar 160 caracteres")
		String title,

		@NotBlank(message = "La descripcion es obligatoria")
		@Size(max = 40_000, message = "La descripcion supera el limite permitido")
		String description,

		@NotNull(message = "El precio es obligatorio")
		@Positive(message = "El precio debe ser mayor a cero")
		BigDecimal price,

		@NotNull(message = "El estado activo es obligatorio")
		Boolean active
) {

	public UpdateProductCommand toCommand(Long productId) {
		return new UpdateProductCommand(productId, categoryId, title, description, price, active);
	}
}
