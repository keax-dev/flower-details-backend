package com.flower_details.features.product.application.dto.command;

import java.util.List;

public record UpdateProductImagePositionsCommand(
		Long productId,
		List<ProductImagePositionCommand> positions
) {
}
