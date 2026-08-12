package com.flower_details.features.product.application.port.out;

import com.flower_details.features.product.domain.model.ProductImage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepositoryPort {

	List<ProductImage> saveAll(List<ProductImage> images);

	void delete(ProductImage image);

	void deleteAllActiveByProductId(Long productId);

	List<ProductImage> findActiveByProductId(Long productId);

	List<ProductImage> findActiveByProductIds(Collection<Long> productIds);

	Optional<ProductImage> findActiveByStoredFileName(String storedFileName);
}
