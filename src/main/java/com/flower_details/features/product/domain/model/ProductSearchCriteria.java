package com.flower_details.features.product.domain.model;

import com.flower_details.shared.domain.DomainException;
import com.flower_details.shared.domain.pagination.SortDirection;

import java.math.BigDecimal;

public record ProductSearchCriteria(
		String query,
		Long categoryId,
		BigDecimal minPrice,
		BigDecimal maxPrice,
		Boolean active,
		ProductSortField sortField,
		SortDirection sortDirection,
		boolean publicCatalog
) {

	public ProductSearchCriteria {
		query = normalizeQuery(query);
		if (categoryId != null && categoryId < 1) throw new DomainException("La categoria debe ser valida");
		if (minPrice != null && minPrice.signum() < 0) throw new DomainException("El precio minimo no puede ser negativo");
		if (maxPrice != null && maxPrice.signum() < 0) throw new DomainException("El precio maximo no puede ser negativo");
		if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
			throw new DomainException("El precio minimo no puede ser mayor al precio maximo");
		}
		sortField = sortField == null ? ProductSortField.CREATED_AT : sortField;
		sortDirection = sortDirection == null ? SortDirection.DESC : sortDirection;
	}

	public static ProductSearchCriteria forCatalog(
			String query, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, String direction
	) {
		return new ProductSearchCriteria(
				query, categoryId, minPrice, maxPrice, Boolean.TRUE,
				ProductSortField.fromApiValue(sortBy), SortDirection.fromApiValue(direction), true
		);
	}

	public static ProductSearchCriteria forManagement(
			String query, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Boolean active, String sortBy, String direction
	) {
		return new ProductSearchCriteria(
				query, categoryId, minPrice, maxPrice, active,
				ProductSortField.fromApiValue(sortBy), SortDirection.fromApiValue(direction), false
		);
	}

	private static String normalizeQuery(String value) {
		if (value == null || value.isBlank()) return null;
		String normalized = value.trim();
		if (normalized.length() > 120) throw new DomainException("La busqueda de productos supera los 120 caracteres");
		return normalized;
	}
}
