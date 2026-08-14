package com.flower_details.features.product.application.dto.view;

import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.model.ProductImage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductView(
		Long id,
		ProductCategoryView category,
		String title,
		String description,
		BigDecimal price,
		boolean active,
		List<ProductImageView> images,
		Instant createdAt,
		Instant updatedAt
) {

	public static ProductView from(Product product, Category category, List<ProductImage> images) {
		return new ProductView(
				product.id(),
				ProductCategoryView.from(category),
				product.title(),
				product.description(),
				product.price(),
				product.active(),
				images.stream().map(ProductImageView::from).toList(),
				product.createdAt(),
				product.updatedAt()
		);
	}
}
