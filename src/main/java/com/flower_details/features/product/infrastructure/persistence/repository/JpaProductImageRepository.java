package com.flower_details.features.product.infrastructure.persistence.repository;

import com.flower_details.features.product.domain.model.ProductImage;
import com.flower_details.features.product.domain.repository.ProductImageRepository;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductImageJpaEntity;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.flower_details.features.product.infrastructure.persistence.mapper.ProductImagePersistenceMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaProductImageRepository implements ProductImageRepository {

	private final SpringDataProductImageJpaRepository repository;
	private final EntityManager entityManager;

	@Override
	public List<ProductImage> saveAll(List<ProductImage> images) {
		return repository.saveAll(images.stream()
						.map(this::toEntity)
						.toList())
				.stream()
				.map(ProductImagePersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public void delete(ProductImage image) {
		repository.findById(image.id()).ifPresent(repository::delete);
	}

	@Override
	public void deleteAllActiveByProductId(Long productId) {
		repository.findAllByProduct_IdAndActiveTrueOrderBySortOrderAsc(productId)
				.forEach(repository::delete);
	}

	@Override
	public List<ProductImage> findActiveByProductId(Long productId) {
		return repository.findAllByProduct_IdAndActiveTrueOrderBySortOrderAsc(productId)
				.stream()
				.map(ProductImagePersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public List<ProductImage> findActiveByProductIds(Collection<Long> productIds) {
		if (productIds.isEmpty()) {
			return List.of();
		}
		return repository.findActiveByProductIds(productIds)
				.stream()
				.map(ProductImagePersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public Optional<ProductImage> findActiveByStoredFileName(String storedFileName) {
		return repository.findByStoredFileNameAndActiveTrue(storedFileName)
				.map(ProductImagePersistenceMapper::toDomain);
	}

	private ProductImageJpaEntity toEntity(ProductImage image) {
		ProductJpaEntity product = entityManager.getReference(ProductJpaEntity.class, image.productId());
		return ProductImagePersistenceMapper.toEntity(image, product);
	}
}
