package com.flower_details.features.cart.domain.model;

import com.flower_details.shared.domain.DomainException;

import java.math.BigDecimal;
import java.time.Instant;

public class CartItem {

	public static final int MAX_QUANTITY = 100;

	private final Long id;
	private final Long cartId;
	private final Long productId;
	private int quantity;
	private final BigDecimal unitPrice;
	private final Instant createdAt;
	private Instant updatedAt;

	private CartItem(Long id, Long cartId, Long productId, int quantity, BigDecimal unitPrice, Instant createdAt, Instant updatedAt) {
		if (cartId == null || productId == null) {
			throw new DomainException("El carrito y el producto son obligatorios");
		}
		if (unitPrice == null || unitPrice.signum() <= 0) {
			throw new DomainException("El precio del producto debe ser mayor a cero");
		}
		this.id = id;
		this.cartId = cartId;
		this.productId = productId;
		this.quantity = requireQuantity(quantity);
		this.unitPrice = unitPrice;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static CartItem create(Long cartId, Long productId, int quantity, BigDecimal unitPrice) {
		return new CartItem(null, cartId, productId, quantity, unitPrice, null, null);
	}

	public static CartItem restore(
			Long id, Long cartId, Long productId, int quantity, BigDecimal unitPrice, Instant createdAt, Instant updatedAt
	) {
		return new CartItem(id, cartId, productId, quantity, unitPrice, createdAt, updatedAt);
	}

	public void addQuantity(int quantity) {
		this.quantity = requireQuantity(Math.addExact(this.quantity, quantity));
	}

	public void updateQuantity(int quantity) {
		this.quantity = requireQuantity(quantity);
	}

	public Long id() { return id; }
	public Long cartId() { return cartId; }
	public Long productId() { return productId; }
	public int quantity() { return quantity; }
	public BigDecimal unitPrice() { return unitPrice; }
	public BigDecimal subtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
	public Instant createdAt() { return createdAt; }
	public Instant updatedAt() { return updatedAt; }

	private static int requireQuantity(int quantity) {
		if (quantity < 1 || quantity > MAX_QUANTITY) {
			throw new DomainException("La cantidad debe estar entre 1 y " + MAX_QUANTITY);
		}
		return quantity;
	}
}
