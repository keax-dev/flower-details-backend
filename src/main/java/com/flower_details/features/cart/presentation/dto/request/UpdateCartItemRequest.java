package com.flower_details.features.cart.presentation.dto.request;

import com.flower_details.features.cart.application.dto.command.UpdateCartItemCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(
		@Min(value = 1, message = "La cantidad debe ser mayor a cero")
		@Max(value = 100, message = "La cantidad maxima es 100") int quantity
) {
	public UpdateCartItemCommand toCommand(Long itemId) {
		return new UpdateCartItemCommand(itemId, quantity);
	}
}
