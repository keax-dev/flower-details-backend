package com.flower_details.features.product.domain.model;

import com.flower_details.shared.domain.DomainException;

import java.time.Instant;

public class ProductImage {

	private static final int FILE_NAME_MAX_LENGTH = 255;
	private static final int CONTENT_TYPE_MAX_LENGTH = 80;
	private static final int URL_MAX_LENGTH = 500;

	private final Long id;
	private final Long productId;
	private final String url;
	private final String storedFileName;
	private final String originalFileName;
	private final String contentType;
	private final long sizeBytes;
	private final int sortOrder;
	private final boolean active;
	private final Instant createdAt;
	private final Instant updatedAt;

	private ProductImage(
			Long id,
			Long productId,
			String url,
			String storedFileName,
			String originalFileName,
			String contentType,
			long sizeBytes,
			int sortOrder,
			boolean active,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = id;
		this.productId = requireId(productId);
		this.url = requireText(url, "La URL de la imagen es obligatoria", URL_MAX_LENGTH);
		this.storedFileName = requireText(storedFileName, "El archivo almacenado es obligatorio", FILE_NAME_MAX_LENGTH);
		this.originalFileName = requireText(originalFileName, "El nombre original de la imagen es obligatorio", FILE_NAME_MAX_LENGTH);
		this.contentType = requireText(contentType, "El tipo de contenido de la imagen es obligatorio", CONTENT_TYPE_MAX_LENGTH);
		this.sizeBytes = requirePositiveSize(sizeBytes);
		this.sortOrder = requireValidSortOrder(sortOrder);
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static ProductImage create(
			Long productId,
			String url,
			String storedFileName,
			String originalFileName,
			String contentType,
			long sizeBytes,
			int sortOrder
	) {
		return new ProductImage(
				null,
				productId,
				url,
				storedFileName,
				originalFileName,
				contentType,
				sizeBytes,
				sortOrder,
				true,
				null,
				null
		);
	}

	public static ProductImage restore(
			Long id,
			Long productId,
			String url,
			String storedFileName,
			String originalFileName,
			String contentType,
			long sizeBytes,
			int sortOrder,
			boolean active,
			Instant createdAt,
			Instant updatedAt
	) {
		return new ProductImage(
				id,
				productId,
				url,
				storedFileName,
				originalFileName,
				contentType,
				sizeBytes,
				sortOrder,
				active,
				createdAt,
				updatedAt
		);
	}

	public Long id() {
		return id;
	}

	public Long productId() {
		return productId;
	}

	public String url() {
		return url;
	}

	public String storedFileName() {
		return storedFileName;
	}

	public String originalFileName() {
		return originalFileName;
	}

	public String contentType() {
		return contentType;
	}

	public long sizeBytes() {
		return sizeBytes;
	}

	public int sortOrder() {
		return sortOrder;
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

	private static Long requireId(Long value) {
		if (value == null) {
			throw new DomainException("El producto de la imagen es obligatorio");
		}
		return value;
	}

	private static long requirePositiveSize(long value) {
		if (value <= 0) {
			throw new DomainException("El tamano de la imagen debe ser mayor a cero");
		}
		return value;
	}

	private static int requireValidSortOrder(int value) {
		if (value < 0) {
			throw new DomainException("El orden de la imagen no puede ser negativo");
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
