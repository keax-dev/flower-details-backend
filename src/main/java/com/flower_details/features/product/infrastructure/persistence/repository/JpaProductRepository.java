package com.flower_details.features.product.infrastructure.persistence.repository;

import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.repository.ProductRepository;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductCategoryReferenceJpaEntity;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.flower_details.features.product.infrastructure.persistence.mapper.ProductPersistenceMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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
	public PageResult<Product> findAllActive(PageRequest pageRequest) {
		Page<ProductJpaEntity> page = repository.findAllByActiveTrueAndCategory_ActiveTrue(
				org.springframework.data.domain.PageRequest.of(
						pageRequest.page(), pageRequest.size(), Sort.by(Sort.Direction.DESC, "createdAt")
				)
		);
		return new PageResult<>(
				page.getContent().stream().map(ProductPersistenceMapper::toDomain).toList(),
				page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
		);
	}
}
