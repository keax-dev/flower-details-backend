package com.flower_details.features.order.domain.model;

import com.flower_details.shared.domain.DomainException;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderItem(Long id, Long orderId, Long productId, String productTitle, String productImageUrl,
		int quantity, BigDecimal unitPrice, BigDecimal subtotal, Instant createdAt) {

	public OrderItem {
		if (orderId == null || productId == null) throw new DomainException("El pedido y producto son obligatorios");
		if (productTitle == null || productTitle.isBlank()) throw new DomainException("El titulo del producto es obligatorio");
		if (quantity < 1 || unitPrice == null || unitPrice.signum() <= 0) throw new DomainException("El item del pedido es invalido");
		subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	public static OrderItem create(Long orderId, Long productId, String productTitle, String productImageUrl, int quantity, BigDecimal unitPrice) {
		return new OrderItem(null, orderId, productId, productTitle.trim(), productImageUrl, quantity, unitPrice, null, null);
	}
}
