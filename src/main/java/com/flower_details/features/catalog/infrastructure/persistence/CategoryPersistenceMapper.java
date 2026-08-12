package com.flower_details.features.catalog.infrastructure.persistence;

import com.flower_details.features.catalog.domain.model.Category;

final class CategoryPersistenceMapper {

	private CategoryPersistenceMapper() {
	}

	static Category toDomain(CategoryJpaEntity entity) {
		return Category.restore(
				entity.getId(),
				entity.getTitle(),
				entity.getDescription(),
				entity.isActive(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	static CategoryJpaEntity toEntity(Category category) {
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
