package com.flower_details.features.product.application.exception;

public class ProductNotFoundException extends RuntimeException {

	public ProductNotFoundException(Long id) {
		super("No existe el producto con id " + id);
	}
}
