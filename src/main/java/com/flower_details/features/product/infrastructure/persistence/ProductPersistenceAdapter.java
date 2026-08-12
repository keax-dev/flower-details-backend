package com.flower_details.features.product.infrastructure.persistence;

import com.flower_details.features.catalog.infrastructure.persistence.CategoryJpaEntity;
import com.flower_details.features.product.application.port.out.ProductRepositoryPort;
import com.flower_details.features.product.domain.model.Product;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class ProductPersistenceAdapter implements ProductRepositoryPort {

	private final SpringDataProductJpaRepository repository;
	private final EntityManager entityManager;

	ProductPersistenceAdapter(SpringDataProductJpaRepository repository, EntityManager entityManager) {
		this.repository = repository;
		this.entityManager = entityManager;
	}

	@Override
	public Product save(Product product) {
		CategoryJpaEntity category = entityManager.getReference(CategoryJpaEntity.class, product.categoryId());
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
