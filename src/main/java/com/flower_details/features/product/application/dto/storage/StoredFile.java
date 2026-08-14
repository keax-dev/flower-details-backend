package com.flower_details.features.product.application.dto.storage;

public record StoredFile(
		String url,
		String storedFileName,
		String originalFileName,
		String contentType,
		long sizeBytes
) {
}
