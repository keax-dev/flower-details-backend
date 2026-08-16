package com.flower_details.features.product.application.usecase;

import com.flower_details.features.category.application.exception.CategoryNotFoundException;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.product.application.dto.query.ProductSearchQuery;
import com.flower_details.features.product.application.dto.storage.StoredFileContent;
import com.flower_details.features.product.application.dto.view.ProductView;
import com.flower_details.features.product.application.exception.ProductImageNotFoundException;
import com.flower_details.features.product.application.exception.ProductNotFoundException;
import com.flower_details.features.product.application.service.ProductImageStorage;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.model.ProductImage;
import com.flower_details.features.product.domain.repository.ProductImageRepository;
import com.flower_details.features.product.domain.repository.ProductRepository;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RetrieveProductsUseCase {
	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final CategoryRepository categoryRepository;
	private final ProductImageStorage productImageStorage;

	@Transactional(readOnly = true)
	public PageResult<ProductView> list(ProductSearchQuery query, PageRequest pageRequest) {
		PageResult<Product> page = productRepository.search(query.toCriteria(), pageRequest);
		Map<Long, Category> categories = categoryRepository.findByIds(page.items().stream().map(Product::categoryId).toList())
				.stream().collect(Collectors.toMap(Category::id, Function.identity()));
		Map<Long, List<ProductImage>> images = productImageRepository.findActiveByProductIds(
				page.items().stream().map(Product::id).toList()
		).stream().collect(Collectors.groupingBy(ProductImage::productId));
		return page.map(product -> ProductView.from(product, category(product.categoryId(), categories),
				images.getOrDefault(product.id(), List.of())));
	}

	@Transactional(readOnly = true)
	public ProductView byId(Long id) {
		Product product = productRepository.findActiveById(id).orElseThrow(() -> new ProductNotFoundException(id));
		return toView(product);
	}

	@Transactional(readOnly = true)
	public ProductView manageById(Long id) {
		Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
		return toView(product);
	}

	private ProductView toView(Product product) {
		Category category = categoryRepository.findById(product.categoryId())
				.orElseThrow(() -> new CategoryNotFoundException(product.categoryId()));
		return ProductView.from(product, category, productImageRepository.findActiveByProductId(product.id()));
	}

	@Transactional(readOnly = true)
	public StoredFileContent imageFile(String storedFileName) {
		ProductImage image = productImageRepository.findActiveByStoredFileName(storedFileName)
				.orElseThrow(() -> new ProductImageNotFoundException(storedFileName));
		return productImageStorage.load(image.storedFileName());
	}

	private Category category(Long id, Map<Long, Category> categories) {
		Category category = categories.get(id);
		if (category == null) throw new CategoryNotFoundException(id);
		return category;
	}
}
