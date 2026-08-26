package com.flower_details.features.category.domain.model;

import com.flower_details.shared.domain.DomainException;

import java.time.Instant;

public class Category {

	private static final int TITLE_MAX_LENGTH = 120;
	private static final int DESCRIPTION_MAX_LENGTH = 20_000;

	private final Long id;
	private String title;
	private String description;
	private boolean active;
	private final Instant createdAt;
	private Instant updatedAt;

	private Category(
			Long id,
			String title,
			String description,
			boolean active,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = id;
		this.title = requireText(title, "El titulo de la categoria es obligatorio", TITLE_MAX_LENGTH);
		this.description = requireText(description, "La descripcion de la categoria es obligatoria", DESCRIPTION_MAX_LENGTH);
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Category create(String title, String description, boolean active) {
		return new Category(null, title, description, active, null, null);
	}

	public static Category restore(
			Long id,
			String title,
			String description,
			boolean active,
			Instant createdAt,
			Instant updatedAt
	) {
		return new Category(id, title, description, active, createdAt, updatedAt);
	}

	public void update(String title, String description, boolean active) {
		this.title = requireText(title, "El titulo de la categoria es obligatorio", TITLE_MAX_LENGTH);
		this.description = requireText(description, "La descripcion de la categoria es obligatoria", DESCRIPTION_MAX_LENGTH);
		this.active = active;
	}

	public Long id() {
		return id;
	}

	public String title() {
		return title;
	}

	public String description() {
		return description;
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
