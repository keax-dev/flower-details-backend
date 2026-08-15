package com.flower_details.features.cart.application.exception;

public class CartItemNotFoundException extends RuntimeException {

	public CartItemNotFoundException(Long id) {
		super("No se encontro el item del carrito con id " + id);
	}
}
