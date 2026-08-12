package com.flower_details.features.product.application.dto;

public record StoredFileContent(
		String fileName,
		String contentType,
		byte[] content
) {
}
