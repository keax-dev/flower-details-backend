package com.flower_details.features.product.infrastructure.persistence.mapper;

import com.flower_details.features.product.domain.model.ProductImage;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductImageJpaEntity;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductJpaEntity;

public final class ProductImagePersistenceMapper {

	private ProductImagePersistenceMapper() {
	}

	public static ProductImage toDomain(ProductImageJpaEntity entity) {
		return ProductImage.restore(
				entity.getId(),
				entity.getProductId(),
				entity.getUrl(),
				entity.getStoredFileName(),
				entity.getOriginalFileName(),
				entity.getContentType(),
				entity.getSizeBytes(),
				entity.getSortOrder(),
				entity.isActive(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	public static ProductImageJpaEntity toEntity(ProductImage image, ProductJpaEntity product) {
		return new ProductImageJpaEntity(
				image.id(),
				product,
				image.url(),
				image.storedFileName(),
				image.originalFileName(),
				image.contentType(),
				image.sizeBytes(),
				image.sortOrder(),
				image.active(),
				image.createdAt(),
				image.updatedAt()
		);
	}
}
