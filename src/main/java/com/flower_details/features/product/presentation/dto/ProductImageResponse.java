package com.flower_details.features.product.presentation.dto;

import com.flower_details.features.product.application.dto.ProductImageView;

public record ProductImageResponse(
		Long id,
		String url,
		String originalFileName,
		String contentType,
		long sizeBytes,
		int sortOrder
) {

	public static ProductImageResponse from(ProductImageView image) {
		return new ProductImageResponse(
				image.id(),
				image.url(),
				image.originalFileName(),
				image.contentType(),
				image.sizeBytes(),
				image.sortOrder()
		);
	}
}
