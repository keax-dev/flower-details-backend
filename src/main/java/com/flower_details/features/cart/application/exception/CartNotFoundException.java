package com.flower_details.features.cart.application.exception;

public class CartNotFoundException extends RuntimeException {

	public CartNotFoundException() {
		super("No existe un carrito activo para el cliente");
	}
}
