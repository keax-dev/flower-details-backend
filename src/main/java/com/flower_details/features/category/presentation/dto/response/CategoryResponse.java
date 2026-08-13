package com.flower_details.features.category.presentation.dto.response;

import com.flower_details.features.category.application.dto.view.CategoryView;

import java.time.Instant;

public record CategoryResponse(
		Long id,
		String title,
		String description,
		boolean active,
		Instant createdAt,
		Instant updatedAt
) {

	public static CategoryResponse from(CategoryView category) {
		return new CategoryResponse(
				category.id(),
				category.title(),
				category.description(),
				category.active(),
				category.createdAt(),
				category.updatedAt()
		);
	}
}
