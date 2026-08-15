package com.flower_details.features.cart.presentation.dto.request;

import com.flower_details.features.cart.application.dto.command.AddCartItemCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
		@NotNull(message = "El producto es obligatorio") Long productId,
		@Min(value = 1, message = "La cantidad debe ser mayor a cero")
		@Max(value = 100, message = "La cantidad maxima es 100") int quantity
) {
	public AddCartItemCommand toCommand() {
		return new AddCartItemCommand(productId, quantity);
	}
}
