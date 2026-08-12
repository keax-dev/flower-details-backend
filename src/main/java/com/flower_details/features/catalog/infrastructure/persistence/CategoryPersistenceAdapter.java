package com.flower_details.features.catalog.infrastructure.persistence;

import com.flower_details.features.catalog.application.port.out.CategoryRepositoryPort;
import com.flower_details.features.catalog.domain.model.Category;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
class CategoryPersistenceAdapter implements CategoryRepositoryPort {

	private final SpringDataCategoryJpaRepository repository;

	CategoryPersistenceAdapter(SpringDataCategoryJpaRepository repository) {
		this.repository = repository;
	}

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
	public boolean existsByTitle(String title) {
		return repository.existsByTitleIgnoreCase(normalizeTitle(title));
	}

	@Override
	public boolean existsByTitleForAnotherCategory(String title, Long categoryId) {
		return repository.existsByTitleIgnoreCaseAndIdNot(normalizeTitle(title), categoryId);
	}

	@Override
	public List<Category> findAllActive() {
		return repository.findAllByActiveTrueOrderByTitleAsc()
				.stream()
				.map(CategoryPersistenceMapper::toDomain)
				.toList();
	}

	private static String normalizeTitle(String title) {
		if (title == null) {
			return "";
		}
		return title.trim().toLowerCase(Locale.ROOT);
	}
}
