package com.flower_details.features.product.application.service;

import com.flower_details.features.product.application.dto.command.CreateProductCommand;
import com.flower_details.features.product.application.dto.command.UpdateProductCommand;
import com.flower_details.features.product.application.dto.query.ProductSearchQuery;
import com.flower_details.features.product.application.dto.view.ProductView;
import com.flower_details.features.category.application.exception.CategoryNotFoundException;
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
	private final ProductImageApplicationService productImageApplicationService;

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
	public void deleteProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException(id));

		productImageApplicationService.deleteAllByProductId(id);
		productRepository.delete(product);
	}

	private ProductView buildView(Product product) {
		return ProductView.from(
				product,
				findCategory(product.categoryId()),
				productImageRepository.findActiveByProductId(product.id())
		);
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

}
