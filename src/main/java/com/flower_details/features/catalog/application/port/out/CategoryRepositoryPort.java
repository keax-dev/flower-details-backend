package com.flower_details.features.catalog.application.port.out;

import com.flower_details.features.catalog.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepositoryPort {

	Category save(Category category);

	void delete(Category category);

	Optional<Category> findById(Long id);

	boolean existsByTitle(String title);

	boolean existsByTitleForAnotherCategory(String title, Long categoryId);

	List<Category> findAllActive();
}
