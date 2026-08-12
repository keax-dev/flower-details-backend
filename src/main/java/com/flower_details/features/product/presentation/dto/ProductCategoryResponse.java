package com.flower_details.features.product.presentation.dto;

import com.flower_details.features.product.application.dto.ProductCategoryView;

public record ProductCategoryResponse(
		Long id,
		String title
) {

	public static ProductCategoryResponse from(ProductCategoryView category) {
		return new ProductCategoryResponse(category.id(), category.title());
	}
}
