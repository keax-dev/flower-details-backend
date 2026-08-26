package com.flower_details.features.product.application.usecase;

import com.flower_details.features.category.application.exception.CategoryNotFoundException;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.product.application.dto.view.ProductView;
import com.flower_details.features.product.application.exception.ProductImageNotFoundException;
import com.flower_details.features.product.application.exception.ProductNotFoundException;
import com.flower_details.features.product.application.service.ProductImageFileLifecycle;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.model.ProductImage;
import com.flower_details.features.product.domain.repository.ProductImageRepository;
import com.flower_details.features.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DeleteProductImageUseCase {

	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final CategoryRepository categoryRepository;
	private final ProductImageFileLifecycle productImageFileLifecycle;

	@Transactional
	public ProductView execute(Long productId, Long imageId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));
		List<ProductImage> images = productImageRepository.findActiveByProductId(productId);
		ProductImage image = images.stream()
				.filter(candidate -> candidate.id().equals(imageId))
				.findFirst()
				.orElseThrow(() -> new ProductImageNotFoundException(imageId));

		productImageRepository.delete(image);
		productImageFileLifecycle.deleteAfterCommit(image.storedFileName());

		List<ProductImage> remainingImages = images.stream()
				.filter(candidate -> !candidate.id().equals(imageId))
				.toList();
		List<ProductImage> reorderedImages = IntStream.range(0, remainingImages.size())
				.mapToObj(index -> remainingImages.get(index)
						.withSortOrder(index))
				.toList();
		List<ProductImage> savedImages = productImageRepository.saveAll(reorderedImages);
		Category category = categoryRepository.findById(product.categoryId())
				.orElseThrow(() -> new CategoryNotFoundException(product.categoryId()));
		return ProductView.from(product, category, savedImages);
	}
}
