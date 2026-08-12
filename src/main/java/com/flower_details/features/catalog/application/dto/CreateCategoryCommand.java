package com.flower_details.features.catalog.application.dto;

public record CreateCategoryCommand(
		String title,
		String description,
		boolean active
) {
}
