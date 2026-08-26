package com.flower_details.features.product.presentation.dto.request;

import com.flower_details.features.product.application.dto.command.UpdateProductImagePositionsCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateProductImagePositionsRequest(
		@NotEmpty(message = "Debes enviar las posiciones de las imagenes")
		List<@Valid ProductImagePositionRequest> positions
) {

	public UpdateProductImagePositionsCommand toCommand(Long productId) {
		return new UpdateProductImagePositionsCommand(
				productId,
				positions.stream().map(ProductImagePositionRequest::toCommand).toList()
		);
	}
}
