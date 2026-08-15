package com.flower_details.features.cart.application.exception;

public class CartProductUnavailableException extends RuntimeException {

	public CartProductUnavailableException(Long productId) {
		super("El producto " + productId + " no esta disponible");
	}
}
