package com.flower_details.features.order.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelOrderRequest(
		@NotBlank
		@Size(max = 500)
		String reason
) {
}
