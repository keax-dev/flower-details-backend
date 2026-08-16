package com.flower_details.shared.domain.pagination;

import com.flower_details.shared.domain.DomainException;

import java.util.Locale;

public enum SortDirection {
	ASC,
	DESC;

	public static SortDirection fromApiValue(String value) {
		if (value == null || value.isBlank()) {
			return DESC;
		}
		try {
			return valueOf(value.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException exception) {
			throw new DomainException("La direccion de ordenamiento debe ser asc o desc");
		}
	}
}
