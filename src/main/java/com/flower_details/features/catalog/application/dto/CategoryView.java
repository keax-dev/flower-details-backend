package com.flower_details.features.catalog.application.dto;

import com.flower_details.features.catalog.domain.model.Category;

import java.time.Instant;

public record CategoryView(
		Long id,
		String title,
		String description,
		boolean active,
		Instant createdAt,
		Instant updatedAt
) {

	public static CategoryView from(Category category) {
		return new CategoryView(
				category.id(),
				category.title(),
				category.description(),
				category.active(),
				category.createdAt(),
				category.updatedAt()
		);
	}
}
