package com.flower_details.features.product.application.exception;

public class ProductImageNotFoundException extends RuntimeException {

	public ProductImageNotFoundException(String storedFileName) {
		super("No existe la imagen " + storedFileName);
	}
}
