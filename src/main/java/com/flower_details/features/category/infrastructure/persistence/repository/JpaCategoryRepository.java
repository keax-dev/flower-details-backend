package com.flower_details.features.category.infrastructure.persistence.repository;

import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.category.infrastructure.persistence.entity.CategoryJpaEntity;
import com.flower_details.features.category.infrastructure.persistence.mapper.CategoryPersistenceMapper;
import lombok.RequiredArgsConstructor;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaCategoryRepository implements CategoryRepository {

	private final SpringDataCategoryJpaRepository repository;

	@Override
	public Category save(Category category) {
		CategoryJpaEntity saved = repository.save(CategoryPersistenceMapper.toEntity(category));
		return CategoryPersistenceMapper.toDomain(saved);
	}

	@Override
	public void delete(Category category) {
		repository.findById(category.id()).ifPresent(repository::delete);
	}

	@Override
	public Optional<Category> findById(Long id) {
		return repository.findById(id).map(CategoryPersistenceMapper::toDomain);
	}

	@Override
	public List<Category> findByIds(Collection<Long> ids) {
		if (ids.isEmpty()) {
			return List.of();
		}
		return repository.findByIdIn(ids)
				.stream()
				.map(CategoryPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public boolean existsByTitle(String title) {
		return repository.existsByTitleIgnoreCase(normalizeTitle(title));
	}

	@Override
	public boolean existsByTitleForAnotherCategory(String title, Long categoryId) {
		return repository.existsByTitleIgnoreCaseAndIdNot(normalizeTitle(title), categoryId);
	}

	@Override
	public PageResult<Category> findAllActive(PageRequest pageRequest) {
		Page<CategoryJpaEntity> page = repository.findAllByActiveTrue(
				toSpringPageRequest(pageRequest)
		);
		return toPageResult(page);
	}

	@Override
	public PageResult<Category> findAll(PageRequest pageRequest) {
		return toPageResult(repository.findAll(toSpringPageRequest(pageRequest)));
	}

	private static org.springframework.data.domain.PageRequest toSpringPageRequest(PageRequest pageRequest) {
		return org.springframework.data.domain.PageRequest.of(
				pageRequest.page(), pageRequest.size(), Sort.by(Sort.Direction.ASC, "title")
		);
	}

	private static PageResult<Category> toPageResult(Page<CategoryJpaEntity> page) {
		return new PageResult<>(
				page.getContent().stream().map(CategoryPersistenceMapper::toDomain).toList(),
				page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
		);
	}

	private static String normalizeTitle(String title) {
		if (title == null) {
			return "";
		}
		return title.trim().toLowerCase(Locale.ROOT);
	}
}
