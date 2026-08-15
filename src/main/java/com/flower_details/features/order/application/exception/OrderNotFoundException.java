package com.flower_details.features.order.application.exception;

public class OrderNotFoundException extends RuntimeException {

	public OrderNotFoundException(Long id) {
		super("No se encontro el pedido con id " + id);
	}
}
