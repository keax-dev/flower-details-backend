package com.flower_details.features.order.domain.model;

import com.flower_details.shared.domain.DomainException;

import java.util.Locale;

public enum OrderSortField {
	CREATED_AT,
	TOTAL,
	STATUS;

	public static OrderSortField fromApiValue(String value) {
		if (value == null || value.isBlank()) {
			return CREATED_AT;
		}
		return switch (value.trim().toLowerCase(Locale.ROOT)) {
			case "createdat", "created_at" -> CREATED_AT;
			case "total" -> TOTAL;
			case "status" -> STATUS;
			default -> throw new DomainException("El campo de ordenamiento de pedidos no es valido");
		};
	}
}
