package com.flower_details.features.category.application.usecase;

import com.flower_details.features.category.application.exception.CategoryHasProductsException;
import com.flower_details.features.category.application.exception.CategoryNotFoundException;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCategoryUseCase {

	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;

	@Transactional
	public void execute(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new CategoryNotFoundException(id));
		if (productRepository.existsByCategoryId(category.id())) {
			throw new CategoryHasProductsException(category.id());
		}
		categoryRepository.delete(category);
	}
}
