package com.flower_details.features.catalog.application.service;

import com.flower_details.features.catalog.application.dto.CategoryView;
import com.flower_details.features.catalog.application.dto.CreateCategoryCommand;
import com.flower_details.features.catalog.application.dto.UpdateCategoryCommand;
import com.flower_details.features.catalog.application.exception.CategoryNotFoundException;
import com.flower_details.features.catalog.application.exception.CategoryTitleAlreadyExistsException;
import com.flower_details.features.catalog.application.port.in.CreateCategoryUseCase;
import com.flower_details.features.catalog.application.port.in.DeleteCategoryUseCase;
import com.flower_details.features.catalog.application.port.in.ListCategoriesUseCase;
import com.flower_details.features.catalog.application.port.in.UpdateCategoryUseCase;
import com.flower_details.features.catalog.application.port.out.CategoryRepositoryPort;
import com.flower_details.features.catalog.domain.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryApplicationService implements
		ListCategoriesUseCase,
		CreateCategoryUseCase,
		UpdateCategoryUseCase,
		DeleteCategoryUseCase {

	private final CategoryRepositoryPort categoryRepository;

	@Override
	@Transactional(readOnly = true)
	public List<CategoryView> listActiveCategories() {
		return categoryRepository.findAllActive()
				.stream()
				.map(CategoryView::from)
				.toList();
	}

	@Override
	@Transactional
	public CategoryView createCategory(CreateCategoryCommand command) {
		if (categoryRepository.existsByTitle(command.title())) {
			throw new CategoryTitleAlreadyExistsException(command.title());
		}

		Category category = Category.create(command.title(), command.description(), command.active());
		return CategoryView.from(categoryRepository.save(category));
	}

	@Override
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

	@Override
	@Transactional
	public void deleteCategory(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new CategoryNotFoundException(id));
		categoryRepository.delete(category);
	}
}
