package com.flower_details.features.product.application.service;

import com.flower_details.features.category.application.exception.CategoryNotFoundException;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.product.application.dto.storage.StoredFile;
import com.flower_details.features.product.application.dto.storage.StoredFileContent;
import com.flower_details.features.product.application.dto.storage.UploadFile;
import com.flower_details.features.product.application.dto.view.ProductView;
import com.flower_details.features.product.application.exception.ProductImageNotFoundException;
import com.flower_details.features.product.application.exception.ProductNotFoundException;
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
public class ProductImageApplicationService {

	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final CategoryRepository categoryRepository;
	private final ProductImageStorage productImageStorage;
	private final ProductImageFileLifecycle productImageFileLifecycle;

	@Transactional
	public ProductView addProductImages(Long productId, List<UploadFile> files) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));
		List<UploadFile> images = requireImages(files);
		List<ProductImage> currentImages = productImageRepository.findActiveByProductId(product.id());
		List<ProductImage> newImages = storeImages(product.id(), images, currentImages.size());

		return ProductView.from(product, findCategory(product.categoryId()), mergeImages(currentImages, newImages));
	}

	@Transactional(readOnly = true)
	public StoredFileContent getProductImageFile(String storedFileName) {
		ProductImage image = productImageRepository.findActiveByStoredFileName(storedFileName)
				.orElseThrow(() -> new ProductImageNotFoundException(storedFileName));
		productRepository.findActiveById(image.productId())
				.orElseThrow(() -> new ProductImageNotFoundException(storedFileName));
		return productImageStorage.load(image.storedFileName());
	}

	public void deleteAllByProductId(Long productId) {
		List<ProductImage> images = productImageRepository.findActiveByProductId(productId);
		productImageRepository.deleteAllActiveByProductId(productId);
		images.forEach(image -> productImageFileLifecycle.deleteAfterCommit(image.storedFileName()));
	}

	private List<ProductImage> storeImages(Long productId, List<UploadFile> files, int initialSortOrder) {
		List<StoredFile> storedFiles = new ArrayList<>();
		try {
			List<ProductImage> images = new ArrayList<>();
			int sortOrder = initialSortOrder;
			for (UploadFile file : files) {
				StoredFile stored = productImageStorage.store(file);
				storedFiles.add(stored);
				productImageFileLifecycle.deleteAfterRollback(stored.storedFileName());
				images.add(ProductImage.create(
						productId,
						stored.url(),
						stored.storedFileName(),
						stored.originalFileName(),
						stored.contentType(),
						stored.sizeBytes(),
						sortOrder++
				));
			}
			return productImageRepository.saveAll(images);
		}
		catch (RuntimeException exception) {
			cleanupStoredFiles(storedFiles);
			throw exception;
		}
	}

	private List<ProductImage> mergeImages(List<ProductImage> currentImages, List<ProductImage> newImages) {
		List<ProductImage> allImages = new ArrayList<>(currentImages);
		allImages.addAll(newImages);
		return allImages;
	}

	private void cleanupStoredFiles(List<StoredFile> storedFiles) {
		for (StoredFile storedFile : storedFiles) {
			try {
				productImageStorage.delete(storedFile.storedFileName());
			}
			catch (RuntimeException exception) {
				log.warn("No se pudo eliminar la imagen almacenada durante la limpieza: {}",
						storedFile.storedFileName(), exception);
			}
		}
	}

	private Category findCategory(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new CategoryNotFoundException(categoryId));
	}

	private List<UploadFile> requireImages(List<UploadFile> files) {
		List<UploadFile> normalized = normalizeFiles(files);
		if (normalized.isEmpty()) {
			throw new DomainException("El producto debe tener al menos una imagen");
		}
		return normalized;
	}

	private List<UploadFile> normalizeFiles(List<UploadFile> files) {
		if (files == null || files.isEmpty()) {
			return List.of();
		}
		return files.stream()
				.filter(file -> file != null && !file.isEmpty())
				.toList();
	}
}
