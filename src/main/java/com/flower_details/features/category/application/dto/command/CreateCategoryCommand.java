package com.flower_details.features.category.application.dto.command;

public record CreateCategoryCommand(
		String title,
		String description,
		boolean active
) {
}


