package com.flower_details.features.product.application.dto.command;

import java.math.BigDecimal;

public record UpdateProductCommand(
		Long id,
		Long categoryId,
		String title,
		String description,
		BigDecimal price,
		boolean active
) {
}
