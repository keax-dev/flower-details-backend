package com.flower_details.features.cart.domain.model;

import com.flower_details.shared.domain.DomainException;

import java.time.Instant;

public class Cart {

	private final Long id;
	private final Long customerId;
	private CartStatus status;
	private final Instant createdAt;
	private Instant updatedAt;

	private Cart(Long id, Long customerId, CartStatus status, Instant createdAt, Instant updatedAt) {
		if (customerId == null) {
			throw new DomainException("El cliente del carrito es obligatorio");
		}
		this.id = id;
		this.customerId = customerId;
		this.status = status == null ? CartStatus.ACTIVE : status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Cart create(Long customerId) {
		return new Cart(null, customerId, CartStatus.ACTIVE, null, null);
	}

	public static Cart restore(Long id, Long customerId, CartStatus status, Instant createdAt, Instant updatedAt) {
		return new Cart(id, customerId, status, createdAt, updatedAt);
	}

	public Long id() { return id; }
	public Long customerId() { return customerId; }
	public CartStatus status() { return status; }
	public Instant createdAt() { return createdAt; }
	public Instant updatedAt() { return updatedAt; }
}
