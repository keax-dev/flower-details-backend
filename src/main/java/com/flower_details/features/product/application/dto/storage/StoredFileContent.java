package com.flower_details.features.product.application.dto.storage;

public record StoredFileContent(
		String fileName,
		String contentType,
		byte[] content
) {
}
