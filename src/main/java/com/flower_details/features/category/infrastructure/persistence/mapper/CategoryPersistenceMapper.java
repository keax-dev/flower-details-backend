package com.flower_details.features.category.infrastructure.persistence.mapper;

import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.infrastructure.persistence.entity.CategoryJpaEntity;

public final class CategoryPersistenceMapper {

	private CategoryPersistenceMapper() {
	}

	public static Category toDomain(CategoryJpaEntity entity) {
		return Category.restore(
				entity.getId(),
				entity.getTitle(),
				entity.getDescription(),
				entity.isActive(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	public static CategoryJpaEntity toEntity(Category category) {
		return new CategoryJpaEntity(
				category.id(),
				category.title(),
				category.description(),
				category.active(),
				category.createdAt(),
				category.updatedAt()
		);
	}
}
