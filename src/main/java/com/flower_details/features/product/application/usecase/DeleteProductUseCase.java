package com.flower_details.features.product.application.usecase;

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

@Service
@RequiredArgsConstructor
public class DeleteProductUseCase {

	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final ProductImageFileLifecycle productImageFileLifecycle;

	@Transactional
	public void execute(Long id) {
		Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
		List<ProductImage> images = productImageRepository.findActiveByProductId(product.id());

		productImageRepository.deleteAllActiveByProductId(product.id());
		images.forEach(image -> productImageFileLifecycle.deleteAfterCommit(image.storedFileName()));
		productRepository.delete(product);
	}
}
