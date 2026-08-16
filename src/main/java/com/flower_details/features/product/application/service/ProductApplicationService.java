package com.flower_details.features.product.application.service;

import com.flower_details.features.product.application.dto.command.CreateProductCommand;
import com.flower_details.features.product.application.dto.command.UpdateProductCommand;
import com.flower_details.features.product.application.dto.query.ProductSearchQuery;
import com.flower_details.features.product.application.dto.storage.StoredFile;
import com.flower_details.features.product.application.dto.storage.StoredFileContent;
import com.flower_details.features.product.application.dto.storage.UploadFile;
import com.flower_details.features.product.application.dto.view.ProductView;
import com.flower_details.features.category.application.exception.CategoryNotFoundException;
import com.flower_details.features.product.application.exception.ProductImageNotFoundException;
import com.flower_details.features.product.application.exception.ProductNotFoundException;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.model.ProductImage;
import com.flower_details.features.product.domain.model.ProductSearchCriteria;
import com.flower_details.features.product.domain.repository.ProductImageRepository;
import com.flower_details.features.product.domain.repository.ProductRepository;
import com.flower_details.shared.domain.DomainException;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;
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
public class ProductApplicationService {

	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final CategoryRepository categoryRepository;
	private final ProductImageStorage productImageStorage;
	private final ProductImageFileLifecycle productImageFileLifecycle;

	@Transactional(readOnly = true)
	public PageResult<ProductView> listProducts(ProductSearchQuery query, PageRequest pageRequest) {
		ProductSearchCriteria criteria = query.toCriteria();
		PageResult<Product> productPage = productRepository.search(criteria, pageRequest);
		List<Product> products = productPage.items();
		Map<Long, Category> categoriesById = findCategoriesById(products.stream()
				.map(Product::categoryId)
				.toList());

		Map<Long, List<ProductImage>> imagesByProductId = productImageRepository.findActiveByProductIds(products.stream()
						.map(Product::id)
						.toList())
				.stream()
				.collect(Collectors.groupingBy(ProductImage::productId));

		return new PageResult<>(products.stream()
				.map(product -> ProductView.from(
						product,
						findCategoryInMap(product.categoryId(), categoriesById),
						imagesByProductId.getOrDefault(product.id(), List.of())
				))
				.toList(), productPage.page(), productPage.size(), productPage.totalElements(), productPage.totalPages());
	}

	@Transactional(readOnly = true)
	public ProductView getActiveProduct(Long id) {
		Product product = productRepository.findActiveById(id)
				.orElseThrow(() -> new ProductNotFoundException(id));
		return buildView(product);
	}

	@Transactional
	public ProductView createProduct(CreateProductCommand command) {
		Category category = findCategory(command.categoryId());

		Product product = Product.create(
				command.categoryId(),
				command.title(),
				command.description(),
				command.price(),
				command.active()
		);
		Product saved = productRepository.save(product);
		return ProductView.from(saved, category, List.of());
	}

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

		return ProductView.from(saved, category, productImageRepository.findActiveByProductId(saved.id()));
	}

	@Transactional
	public ProductView addProductImages(Long productId, List<UploadFile> files) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));
		List<UploadFile> images = requireImages(files);
		List<ProductImage> currentImages = productImageRepository.findActiveByProductId(product.id());
		List<ProductImage> newImages = storeImages(product.id(), images, currentImages.size());

		return ProductView.from(product, findCategory(product.categoryId()), mergeImages(currentImages, newImages));
	}

	@Transactional
	public void deleteProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException(id));

		List<ProductImage> images = productImageRepository.findActiveByProductId(id);
		productImageRepository.deleteAllActiveByProductId(id);
		productRepository.delete(product);
		images.forEach(image -> productImageFileLifecycle.deleteAfterCommit(image.storedFileName()));
	}

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
