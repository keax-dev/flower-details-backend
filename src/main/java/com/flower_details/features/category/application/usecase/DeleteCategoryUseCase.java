package com.flower_details.features.category.application.usecase;

import com.flower_details.features.category.application.exception.CategoryNotFoundException;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCategoryUseCase {

	private final CategoryRepository categoryRepository;

	@Transactional
	public void execute(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new CategoryNotFoundException(id));
		categoryRepository.delete(category);
	}
}
