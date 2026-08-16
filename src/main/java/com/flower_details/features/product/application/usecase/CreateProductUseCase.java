package com.flower_details.features.product.application.usecase;

import com.flower_details.features.category.application.exception.CategoryNotFoundException;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.product.application.dto.command.CreateProductCommand;
import com.flower_details.features.product.application.dto.view.ProductView;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateProductUseCase {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;

	@Transactional
	public ProductView execute(CreateProductCommand command) {
		Category category = categoryRepository.findById(command.categoryId())
				.orElseThrow(() -> new CategoryNotFoundException(command.categoryId()));
		Product product = Product.create(
				command.categoryId(), command.title(), command.description(), command.price(), command.active()
		);
		return ProductView.from(productRepository.save(product), category, List.of());
	}
}
