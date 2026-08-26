package com.flower_details.features.product.application.exception;

public class ProductImageNotFoundException extends RuntimeException {

	public ProductImageNotFoundException(String storedFileName) {
		super("No existe la imagen " + storedFileName);
	}

	public ProductImageNotFoundException(Long imageId) {
		super("No existe la imagen con id " + imageId);
	}
}
