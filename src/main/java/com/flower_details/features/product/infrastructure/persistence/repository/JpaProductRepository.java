package com.flower_details.features.product.infrastructure.persistence.repository;

import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.model.ProductSearchCriteria;
import com.flower_details.features.product.domain.model.ProductSortField;
import com.flower_details.features.product.domain.repository.ProductRepository;
import com.flower_details.features.category.infrastructure.persistence.entity.CategoryJpaEntity;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.flower_details.features.product.infrastructure.persistence.mapper.ProductPersistenceMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Locale;

import jakarta.persistence.criteria.Predicate;

@Repository
@RequiredArgsConstructor
class JpaProductRepository implements ProductRepository {

	private final SpringDataProductJpaRepository repository;
	private final EntityManager entityManager;

	@Override
	public Product save(Product product) {
		CategoryJpaEntity category = entityManager.getReference(
				CategoryJpaEntity.class,
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
	public boolean existsByCategoryId(Long categoryId) {
		return repository.existsByCategory_Id(categoryId);
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
	public List<Product> findByIds(Collection<Long> ids) {
		if (ids.isEmpty()) {
			return List.of();
		}
		return repository.findByIdIn(ids).stream().map(ProductPersistenceMapper::toDomain).toList();
	}

	@Override
	public List<Product> findActiveByIds(Collection<Long> ids) {
		if (ids.isEmpty()) {
			return List.of();
		}
		return repository.findByIdInAndActiveTrueAndCategory_ActiveTrue(ids).stream()
				.map(ProductPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public PageResult<Product> search(ProductSearchCriteria criteria, PageRequest pageRequest) {
		Page<ProductJpaEntity> page = repository.findAll(specification(criteria), pageable(pageRequest, criteria));
		return new PageResult<>(
				page.getContent().stream().map(ProductPersistenceMapper::toDomain).toList(),
				page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
		);
	}

	private Specification<ProductJpaEntity> specification(ProductSearchCriteria criteria) {
		return (root, query, builder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (criteria.publicCatalog()) {
				predicates.add(builder.isTrue(root.get("active")));
				predicates.add(builder.isTrue(root.join("category").get("active")));
			}
			else if (criteria.active() != null) {
				predicates.add(builder.equal(root.get("active"), criteria.active()));
			}
			if (criteria.categoryId() != null) {
				predicates.add(builder.equal(root.get("category").get("id"), criteria.categoryId()));
			}
			if (criteria.minPrice() != null) {
				predicates.add(builder.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
			}
			if (criteria.maxPrice() != null) {
				predicates.add(builder.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
			}
			if (criteria.query() != null) {
				String pattern = "%" + criteria.query().toLowerCase(Locale.ROOT) + "%";
				predicates.add(builder.or(
						builder.like(builder.lower(root.get("title")), pattern),
						builder.like(builder.lower(root.get("description")), pattern)
				));
			}
			return builder.and(predicates.toArray(Predicate[]::new));
		};
	}

	private org.springframework.data.domain.Pageable pageable(PageRequest pageRequest, ProductSearchCriteria criteria) {
		Sort.Direction direction = criteria.sortDirection() == com.flower_details.shared.domain.pagination.SortDirection.ASC
				? Sort.Direction.ASC
				: Sort.Direction.DESC;
		String property = switch (criteria.sortField()) {
			case ProductSortField.TITLE -> "title";
			case ProductSortField.PRICE -> "price";
			case ProductSortField.CREATED_AT -> "createdAt";
		};
		return org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size(), Sort.by(direction, property));
	}
}
