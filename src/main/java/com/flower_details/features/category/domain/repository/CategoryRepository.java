package com.flower_details.features.category.domain.repository;

import com.flower_details.features.category.domain.model.Category;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

	Category save(Category category);

	void delete(Category category);

	Optional<Category> findById(Long id);

	List<Category> findByIds(Collection<Long> ids);

	boolean existsByTitle(String title);

	boolean existsByTitleForAnotherCategory(String title, Long categoryId);

	PageResult<Category> findAllActive(PageRequest pageRequest);

	PageResult<Category> findAll(PageRequest pageRequest);
}
