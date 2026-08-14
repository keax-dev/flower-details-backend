package com.flower_details.features.product.infrastructure.persistence.repository;

import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.repository.ProductRepository;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductCategoryReferenceJpaEntity;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.flower_details.features.product.infrastructure.persistence.mapper.ProductPersistenceMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaProductRepository implements ProductRepository {

	private final SpringDataProductJpaRepository repository;
	private final EntityManager entityManager;

	@Override
	public Product save(Product product) {
		ProductCategoryReferenceJpaEntity category = entityManager.getReference(
				ProductCategoryReferenceJpaEntity.class,
				product.categoryId()
		);
		ProductJpaEntity saved = repository.save(ProductPersistenceMapper.toEntity(product, category));
		return ProductPersistenceMapper.toDomain(saved);
	}

	@Override
	public void delete(Product product) {
		repository.findById(product.id()).ifPresent(repository::delete);
	}

	@Override
	public Optional<Product> findById(Long id) {
		return repository.findById(id).map(ProductPersistenceMapper::toDomain);
	}

	@Override
	public Optional<Product> findActiveById(Long id) {
		return repository.findByIdAndActiveTrueAndCategory_ActiveTrue(id).map(ProductPersistenceMapper::toDomain);
	}

	@Override
	public List<Product> findAllActive() {
		return repository.findAllByActiveTrueAndCategory_ActiveTrueOrderByCreatedAtDesc()
				.stream()
				.map(ProductPersistenceMapper::toDomain)
				.toList();
	}
}
