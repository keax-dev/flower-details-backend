package com.flower_details.features.product.application.usecase;

import com.flower_details.features.category.application.exception.CategoryNotFoundException;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.product.application.dto.command.UpdateProductCommand;
import com.flower_details.features.product.application.dto.view.ProductView;
import com.flower_details.features.product.application.exception.ProductNotFoundException;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.repository.ProductImageRepository;
import com.flower_details.features.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProductUseCase {

	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final CategoryRepository categoryRepository;

	@Transactional
	public ProductView execute(UpdateProductCommand command) {
		Product product = productRepository.findById(command.id())
				.orElseThrow(() -> new ProductNotFoundException(command.id()));
		Category category = categoryRepository.findById(command.categoryId())
				.orElseThrow(() -> new CategoryNotFoundException(command.categoryId()));
		product.update(command.categoryId(), command.title(), command.description(), command.price(), command.active());
		Product saved = productRepository.save(product);
		return ProductView.from(saved, category, productImageRepository.findActiveByProductId(saved.id()));
	}
}
