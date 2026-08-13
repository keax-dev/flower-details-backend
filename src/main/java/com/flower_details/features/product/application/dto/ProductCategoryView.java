package com.flower_details.features.product.application.dto;

import com.flower_details.features.category.domain.model.Category;

public record ProductCategoryView(
		Long id,
		String title
) {

	public static ProductCategoryView from(Category category) {
		return new ProductCategoryView(category.id(), category.title());
	}
}
