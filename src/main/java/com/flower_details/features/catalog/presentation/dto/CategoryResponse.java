package com.flower_details.features.catalog.presentation.dto;

import com.flower_details.features.catalog.application.dto.CategoryView;

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
