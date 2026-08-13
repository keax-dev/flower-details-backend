package com.flower_details.features.product.infrastructure.persistence;

import com.flower_details.features.product.domain.model.Product;

final class ProductPersistenceMapper {

	private ProductPersistenceMapper() {
	}

	static Product toDomain(ProductJpaEntity entity) {
		return Product.restore(
				entity.getId(),
				entity.getCategoryId(),
				entity.getTitle(),
				entity.getDescription(),
				entity.getPrice(),
				entity.isActive(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	static ProductJpaEntity toEntity(Product product, ProductCategoryReferenceJpaEntity category) {
		return new ProductJpaEntity(
				product.id(),
				category,
				product.title(),
				product.description(),
				product.price(),
				product.active(),
				product.createdAt(),
				product.updatedAt()
		);
	}
}
