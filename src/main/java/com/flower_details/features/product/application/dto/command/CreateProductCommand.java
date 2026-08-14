package com.flower_details.features.product.application.dto.command;

import com.flower_details.features.product.application.dto.storage.UploadFile;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductCommand(
		Long categoryId,
		String title,
		String description,
		BigDecimal price,
		boolean active,
		List<UploadFile> images
) {
}
