package com.flower_details.features.product.domain.repository;

import com.flower_details.features.product.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

	Product save(Product product);

	void delete(Product product);

	Optional<Product> findById(Long id);

	Optional<Product> findActiveById(Long id);

	List<Product> findAllActive();
}
