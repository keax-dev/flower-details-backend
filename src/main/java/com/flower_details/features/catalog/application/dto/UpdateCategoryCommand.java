package com.flower_details.features.catalog.application.dto;

public record UpdateCategoryCommand(
		Long id,
		String title,
		String description,
		boolean active
) {
}
