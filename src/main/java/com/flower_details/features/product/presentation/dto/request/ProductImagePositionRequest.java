package com.flower_details.features.product.presentation.dto.request;

import com.flower_details.features.product.application.dto.command.ProductImagePositionCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductImagePositionRequest(
		@NotNull(message = "La imagen es obligatoria")
		@Positive(message = "La imagen debe ser valida")
		Long imageId,

		@NotNull(message = "La posicion es obligatoria")
		@PositiveOrZero(message = "La posicion no puede ser negativa")
		Integer sortOrder
) {

	public ProductImagePositionCommand toCommand() {
		return new ProductImagePositionCommand(imageId, sortOrder);
	}
}
