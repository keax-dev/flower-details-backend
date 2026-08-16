package com.flower_details.features.product.application.usecase;

import com.flower_details.features.product.application.exception.ProductNotFoundException;
import com.flower_details.features.product.application.service.ProductImageFileLifecycle;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.repository.ProductImageRepository;
import com.flower_details.features.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteProductUseCase {
	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final ProductImageFileLifecycle productImageFileLifecycle;

	@Transactional
	public void execute(Long id) {
		Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
		productImageRepository.findActiveByProductId(id)
				.forEach(image -> productImageFileLifecycle.deleteAfterCommit(image.storedFileName()));
		productImageRepository.deleteAllActiveByProductId(id);
		productRepository.delete(product);
	}
}
