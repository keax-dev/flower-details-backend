package com.flower_details.features.product.presentation.dto.response;

import com.flower_details.features.product.application.dto.view.ProductView;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductResponse(
		Long id,
		ProductCategoryResponse category,
		String title,
		String description,
		BigDecimal price,
		boolean active,
		List<ProductImageResponse> images,
		Instant createdAt,
		Instant updatedAt
) {

	public static ProductResponse from(ProductView product) {
		return new ProductResponse(
				product.id(),
				ProductCategoryResponse.from(product.category()),
				product.title(),
				product.description(),
				product.price(),
				product.active(),
				product.images().stream().map(ProductImageResponse::from).toList(),
				product.createdAt(),
				product.updatedAt()
		);
	}
}
