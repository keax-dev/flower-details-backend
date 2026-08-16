package com.flower_details.features.product.application.usecase;

import com.flower_details.features.category.application.exception.CategoryNotFoundException;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.product.application.dto.storage.StoredFile;
import com.flower_details.features.product.application.dto.storage.UploadFile;
import com.flower_details.features.product.application.dto.view.ProductView;
import com.flower_details.features.product.application.exception.ProductNotFoundException;
import com.flower_details.features.product.application.service.ProductImageFileLifecycle;
import com.flower_details.features.product.application.service.ProductImageStorage;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.model.ProductImage;
import com.flower_details.features.product.domain.repository.ProductImageRepository;
import com.flower_details.features.product.domain.repository.ProductRepository;
import com.flower_details.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateProductImagesUseCase {
	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final CategoryRepository categoryRepository;
	private final ProductImageStorage productImageStorage;
	private final ProductImageFileLifecycle productImageFileLifecycle;

	@Transactional
	public ProductView execute(Long productId, List<UploadFile> files) {
		Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
		List<UploadFile> images = files == null ? List.of() : files.stream().filter(file -> file != null && !file.isEmpty()).toList();
		if (images.isEmpty()) throw new DomainException("El producto debe tener al menos una imagen");
		List<ProductImage> current = new ArrayList<>(productImageRepository.findActiveByProductId(productId));
		List<StoredFile> storedFiles = new ArrayList<>();
		try {
			List<ProductImage> newImages = new ArrayList<>();
			int sortOrder = current.size();
			for (UploadFile file : images) {
				StoredFile stored = productImageStorage.store(file);
				storedFiles.add(stored);
				productImageFileLifecycle.deleteAfterRollback(stored.storedFileName());
				newImages.add(ProductImage.create(productId, stored.url(), stored.storedFileName(),
						stored.originalFileName(), stored.contentType(), stored.sizeBytes(), sortOrder++));
			}
			current.addAll(productImageRepository.saveAll(newImages));
			Category category = categoryRepository.findById(product.categoryId())
					.orElseThrow(() -> new CategoryNotFoundException(product.categoryId()));
			return ProductView.from(product, category, current);
		} catch (RuntimeException exception) {
			storedFiles.forEach(stored -> {
				try { productImageStorage.delete(stored.storedFileName()); }
				catch (RuntimeException cleanupException) { log.warn("No se pudo eliminar la imagen {}", stored.storedFileName(), cleanupException); }
			});
			throw exception;
		}
	}
}
