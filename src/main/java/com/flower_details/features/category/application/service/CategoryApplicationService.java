package com.flower_details.features.category.application.service;

import com.flower_details.features.category.application.dto.command.CreateCategoryCommand;
import com.flower_details.features.category.application.dto.command.UpdateCategoryCommand;
import com.flower_details.features.category.application.dto.view.CategoryView;
import com.flower_details.features.category.application.exception.CategoryNotFoundException;
import com.flower_details.features.category.application.exception.CategoryTitleAlreadyExistsException;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CategoryApplicationService {

	private final CategoryRepository categoryRepository;

	@Transactional(readOnly = true)
	public PageResult<CategoryView> listActiveCategories(PageRequest pageRequest) {
		return categoryRepository.findAllActive(pageRequest).map(CategoryView::from);
	}

	@Transactional
	public CategoryView createCategory(CreateCategoryCommand command) {
		if (categoryRepository.existsByTitle(command.title())) {
			throw new CategoryTitleAlreadyExistsException(command.title());
		}

		Category category = Category.create(command.title(), command.description(), command.active());
		return CategoryView.from(categoryRepository.save(category));
	}

	@Transactional
	public CategoryView updateCategory(UpdateCategoryCommand command) {
		Category category = categoryRepository.findById(command.id())
				.orElseThrow(() -> new CategoryNotFoundException(command.id()));

		if (categoryRepository.existsByTitleForAnotherCategory(command.title(), command.id())) {
			throw new CategoryTitleAlreadyExistsException(command.title());
		}

		category.update(command.title(), command.description(), command.active());
		return CategoryView.from(categoryRepository.save(category));
	}

	@Transactional
	public void deleteCategory(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new CategoryNotFoundException(id));
		categoryRepository.delete(category);
	}
}
