package com.flower_details.features.product.domain.model;

import com.flower_details.shared.domain.DomainException;

import java.util.Locale;

public enum ProductSortField {
	TITLE,
	PRICE,
	CREATED_AT;

	public static ProductSortField fromApiValue(String value) {
		if (value == null || value.isBlank()) {
			return CREATED_AT;
		}
		return switch (value.trim().toLowerCase(Locale.ROOT)) {
			case "title" -> TITLE;
			case "price" -> PRICE;
			case "createdat", "created_at" -> CREATED_AT;
			default -> throw new DomainException("El campo de ordenamiento de productos no es valido");
		};
	}
}
