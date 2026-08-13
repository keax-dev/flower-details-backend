package com.flower_details.features.category.application.dto.command;

public record UpdateCategoryCommand(
		Long id,
		String title,
		String description,
		boolean active
) {
}
