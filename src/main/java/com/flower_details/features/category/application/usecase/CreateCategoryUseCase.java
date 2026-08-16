package com.flower_details.features.category.application.usecase;

import com.flower_details.features.category.application.dto.command.CreateCategoryCommand;
import com.flower_details.features.category.application.dto.view.CategoryView;
import com.flower_details.features.category.application.exception.CategoryTitleAlreadyExistsException;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCategoryUseCase {

	private final CategoryRepository categoryRepository;

	@Transactional
	public CategoryView execute(CreateCategoryCommand command) {
		if (categoryRepository.existsByTitle(command.title())) {
			throw new CategoryTitleAlreadyExistsException(command.title());
		}
		return CategoryView.from(categoryRepository.save(
				Category.create(command.title(), command.description(), command.active())
		));
	}
}
