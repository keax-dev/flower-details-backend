package com.flower_details.features.product.application.dto.view;

import com.flower_details.features.product.domain.model.ProductImage;

public record ProductImageView(
		Long id,
		String url,
		String originalFileName,
		String contentType,
		long sizeBytes,
		int sortOrder
) {

	public static ProductImageView from(ProductImage image) {
		return new ProductImageView(
				image.id(),
				image.url(),
				image.originalFileName(),
				image.contentType(),
				image.sizeBytes(),
				image.sortOrder()
		);
	}
}
