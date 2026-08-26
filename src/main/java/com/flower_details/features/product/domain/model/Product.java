package com.flower_details.features.product.domain.model;

import com.flower_details.shared.domain.DomainException;

import java.math.BigDecimal;
import java.time.Instant;

public class Product {

	private static final int TITLE_MAX_LENGTH = 160;
	private static final int DESCRIPTION_MAX_LENGTH = 40_000;

	private final Long id;
	private Long categoryId;
	private String title;
	private String description;
	private BigDecimal price;
	private boolean active;
	private final Instant createdAt;
	private Instant updatedAt;

	private Product(
			Long id,
			Long categoryId,
			String title,
			String description,
			BigDecimal price,
			boolean active,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = id;
		this.categoryId = requireId(categoryId, "La categoria del producto es obligatoria");
		this.title = requireText(title, "El titulo del producto es obligatorio", TITLE_MAX_LENGTH);
		this.description = requireText(description, "La descripcion del producto es obligatoria", DESCRIPTION_MAX_LENGTH);
		this.price = requirePositivePrice(price);
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Product create(
			Long categoryId,
			String title,
			String description,
			BigDecimal price,
			boolean active
	) {
		return new Product(null, categoryId, title, description, price, active, null, null);
	}

	public static Product restore(
			Long id,
			Long categoryId,
			String title,
			String description,
			BigDecimal price,
			boolean active,
			Instant createdAt,
			Instant updatedAt
	) {
		return new Product(id, categoryId, title, description, price, active, createdAt, updatedAt);
	}

	public void update(
			Long categoryId,
			String title,
			String description,
			BigDecimal price,
			boolean active
	) {
		this.categoryId = requireId(categoryId, "La categoria del producto es obligatoria");
		this.title = requireText(title, "El titulo del producto es obligatorio", TITLE_MAX_LENGTH);
		this.description = requireText(description, "La descripcion del producto es obligatoria", DESCRIPTION_MAX_LENGTH);
		this.price = requirePositivePrice(price);
		this.active = active;
	}

	public void deactivate() {
		active = false;
	}

	public Long id() {
		return id;
	}

	public Long categoryId() {
		return categoryId;
	}

	public String title() {
		return title;
	}

	public String description() {
		return description;
	}

	public BigDecimal price() {
		return price;
	}

	public boolean active() {
		return active;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}

	private static Long requireId(Long value, String message) {
		if (value == null) {
			throw new DomainException(message);
		}
		return value;
	}

	private static BigDecimal requirePositivePrice(BigDecimal value) {
		if (value == null) {
			throw new DomainException("El precio del producto es obligatorio");
		}
		if (value.signum() <= 0) {
			throw new DomainException("El precio del producto debe ser mayor a cero");
		}
		return value;
	}

	private static String requireText(String value, String message, int maxLength) {
		if (value == null || value.isBlank()) {
			throw new DomainException(message);
		}
		String normalized = value.trim();
		if (normalized.length() > maxLength) {
			throw new DomainException("El texto supera el maximo de " + maxLength + " caracteres");
		}
		return normalized;
	}
}
