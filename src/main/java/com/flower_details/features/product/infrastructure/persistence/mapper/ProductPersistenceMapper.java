package com.flower_details.features.product.infrastructure.persistence.mapper;

import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductCategoryReferenceJpaEntity;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductJpaEntity;

public final class ProductPersistenceMapper {

	private ProductPersistenceMapper() {
	}

	public static Product toDomain(ProductJpaEntity entity) {
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

	public static ProductJpaEntity toEntity(Product product, ProductCategoryReferenceJpaEntity category) {
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
