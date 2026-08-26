package com.flower_details.features.category.application.usecase;

import com.flower_details.features.category.application.dto.command.UpdateCategoryCommand;
import com.flower_details.features.category.application.dto.view.CategoryView;
import com.flower_details.features.category.application.exception.CategoryNotFoundException;
import com.flower_details.features.category.application.exception.CategoryTitleAlreadyExistsException;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.shared.domain.content.RichTextSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCategoryUseCase {

	private final CategoryRepository categoryRepository;
	private final RichTextSanitizer richTextSanitizer;

	@Transactional
	public CategoryView execute(UpdateCategoryCommand command) {
		Category category = categoryRepository.findById(command.id())
				.orElseThrow(() -> new CategoryNotFoundException(command.id()));
		if (categoryRepository.existsByTitleForAnotherCategory(command.title(), command.id())) {
			throw new CategoryTitleAlreadyExistsException(command.title());
		}
		category.update(
				command.title(),
				richTextSanitizer.sanitizeDescription(command.description(), 500),
				command.active()
		);
		return CategoryView.from(categoryRepository.save(category));
	}
}
