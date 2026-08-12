package com.flower_details.features.product.application.service;

import com.flower_details.features.product.application.dto.CreateProductCommand;
import com.flower_details.features.product.application.dto.ProductView;
import com.flower_details.features.product.application.dto.StoredFile;
import com.flower_details.features.product.application.dto.StoredFileContent;
import com.flower_details.features.product.application.dto.UpdateProductCommand;
import com.flower_details.features.product.application.dto.UploadFile;
import com.flower_details.features.catalog.application.exception.CategoryNotFoundException;
import com.flower_details.features.product.application.exception.ProductImageNotFoundException;
import com.flower_details.features.product.application.exception.ProductNotFoundException;
import com.flower_details.features.product.application.port.in.CreateProductUseCase;
import com.flower_details.features.product.application.port.in.DeleteProductUseCase;
import com.flower_details.features.product.application.port.in.GetProductImageFileUseCase;
import com.flower_details.features.product.application.port.in.GetProductUseCase;
import com.flower_details.features.product.application.port.in.ListProductsUseCase;
import com.flower_details.features.product.application.port.in.UpdateProductUseCase;
import com.flower_details.features.catalog.application.port.out.CategoryRepositoryPort;
import com.flower_details.features.product.application.port.out.ProductImageRepositoryPort;
import com.flower_details.features.product.application.port.out.ProductImageStoragePort;
import com.flower_details.features.product.application.port.out.ProductRepositoryPort;
import com.flower_details.features.catalog.domain.model.Category;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.model.ProductImage;
import com.flower_details.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductApplicationService implements
		ListProductsUseCase,
		GetProductUseCase,
		CreateProductUseCase,
		UpdateProductUseCase,
		DeleteProductUseCase,
		GetProductImageFileUseCase {

	private final ProductRepositoryPort productRepository;
	private final ProductImageRepositoryPort productImageRepository;
	private final CategoryRepositoryPort categoryRepository;
	private final ProductImageStoragePort productImageStorage;

	@Override
	@Transactional(readOnly = true)
	public List<ProductView> listActiveProducts() {
		List<Product> products = productRepository.findAllActive();
		Map<Long, Category> categoriesById = findCategoriesById(products.stream()
				.map(Product::categoryId)
				.toList());
		Map<Long, List<ProductImage>> imagesByProductId = productImageRepository.findActiveByProductIds(products.stream()
						.map(Product::id)
						.toList())
				.stream()
				.collect(Collectors.groupingBy(ProductImage::productId));

		return products.stream()
				.map(product -> ProductView.from(
						product,
						findCategoryInMap(product.categoryId(), categoriesById),
						imagesByProductId.getOrDefault(product.id(), List.of())
				))
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public ProductView getActiveProduct(Long id) {
		Product product = productRepository.findActiveById(id)
				.orElseThrow(() -> new ProductNotFoundException(id));
		return buildView(product);
	}

	@Override
	@Transactional
	public ProductView createProduct(CreateProductCommand command) {
		Category category = findCategory(command.categoryId());
		List<UploadFile> images = requireImages(command.images());

		Product product = Product.create(
				command.categoryId(),
				command.title(),
				command.description(),
				command.price(),
				command.active()
		);
		Product saved = productRepository.save(product);
		List<ProductImage> savedImages = storeImages(saved.id(), images);

		return ProductView.from(saved, category, savedImages);
	}

	@Override
	@Transactional
	public ProductView updateProduct(UpdateProductCommand command) {
		Product product = productRepository.findById(command.id())
				.orElseThrow(() -> new ProductNotFoundException(command.id()));
		Category category = findCategory(command.categoryId());

		product.update(
				command.categoryId(),
				command.title(),
				command.description(),
				command.price(),
				command.active()
		);
		Product saved = productRepository.save(product);

		List<UploadFile> newImages = normalizeFiles(command.images());
		if (!newImages.isEmpty()) {
			productImageRepository.deleteAllActiveByProductId(saved.id());
			storeImages(saved.id(), newImages);
		}

		return ProductView.from(saved, category, productImageRepository.findActiveByProductId(saved.id()));
	}

	@Override
	@Transactional
	public void deleteProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException(id));

		productImageRepository.deleteAllActiveByProductId(id);
		productRepository.delete(product);
	}

	@Override
	@Transactional(readOnly = true)
	public StoredFileContent getProductImageFile(String storedFileName) {
		ProductImage image = productImageRepository.findActiveByStoredFileName(storedFileName)
				.orElseThrow(() -> new ProductImageNotFoundException(storedFileName));
		productRepository.findActiveById(image.productId())
				.orElseThrow(() -> new ProductImageNotFoundException(storedFileName));
		return productImageStorage.load(image.storedFileName());
	}

	private ProductView buildView(Product product) {
		return ProductView.from(
				product,
				findCategory(product.categoryId()),
				productImageRepository.findActiveByProductId(product.id())
		);
	}

	private List<ProductImage> storeImages(Long productId, List<UploadFile> files) {
		List<StoredFile> storedFiles = new ArrayList<>();
		try {
			List<ProductImage> images = new ArrayList<>();
			int sortOrder = 0;
			for (UploadFile file : files) {
				StoredFile stored = productImageStorage.store(file);
				storedFiles.add(stored);
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

	private void cleanupStoredFiles(List<StoredFile> storedFiles) {
		for (StoredFile storedFile : storedFiles) {
			try {
				productImageStorage.delete(storedFile.storedFileName());
			}
			catch (RuntimeException ignored) {
			}
		}
	}

	private Category findCategory(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new CategoryNotFoundException(categoryId));
	}

	private Map<Long, Category> findCategoriesById(Collection<Long> categoryIds) {
		return categoryRepository.findByIds(categoryIds)
				.stream()
				.collect(Collectors.toMap(Category::id, Function.identity()));
	}

	private Category findCategoryInMap(Long categoryId, Map<Long, Category> categoriesById) {
		Category category = categoriesById.get(categoryId);
		if (category == null) {
			throw new CategoryNotFoundException(categoryId);
		}
		return category;
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
