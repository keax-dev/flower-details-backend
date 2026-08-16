package com.flower_details.features.product.application.dto.query;

import com.flower_details.features.product.domain.model.ProductSearchCriteria;
import com.flower_details.features.product.domain.model.ProductSortField;
import com.flower_details.shared.domain.DomainException;
import com.flower_details.shared.domain.pagination.SortDirection;

import java.math.BigDecimal;
import java.util.Locale;

public record ProductSearchQuery(
		String query,
		Long categoryId,
		BigDecimal minPrice,
		BigDecimal maxPrice,
		Boolean active,
		String sortBy,
		String direction,
		boolean publicCatalog
) {

	public static ProductSearchQuery forCatalog(
			String query,
			Long categoryId,
			BigDecimal minPrice,
			BigDecimal maxPrice,
			String sortBy,
			String direction
	) {
		return new ProductSearchQuery(query, categoryId, minPrice, maxPrice, true, sortBy, direction, true);
	}

	public static ProductSearchQuery forManagement(
			String query,
			Long categoryId,
			BigDecimal minPrice,
			BigDecimal maxPrice,
			Boolean active,
			String sortBy,
			String direction
	) {
		return new ProductSearchQuery(query, categoryId, minPrice, maxPrice, active, sortBy, direction, false);
	}

	public ProductSearchCriteria toCriteria() {
		return new ProductSearchCriteria(
				query,
				categoryId,
				minPrice,
				maxPrice,
				active,
				toSortField(sortBy),
				toSortDirection(direction),
				publicCatalog
		);
	}

	private static ProductSortField toSortField(String value) {
		if (value == null || value.isBlank()) {
			return ProductSortField.CREATED_AT;
		}
		return switch (value.trim().toLowerCase(Locale.ROOT)) {
			case "title" -> ProductSortField.TITLE;
			case "price" -> ProductSortField.PRICE;
			case "createdat", "created_at" -> ProductSortField.CREATED_AT;
			default -> throw new DomainException("El campo de ordenamiento de productos no es valido");
		};
	}

	private static SortDirection toSortDirection(String value) {
		if (value == null || value.isBlank()) {
			return SortDirection.DESC;
		}
		try {
			return SortDirection.valueOf(value.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException exception) {
			throw new DomainException("La direccion de ordenamiento debe ser asc o desc");
		}
	}
}
