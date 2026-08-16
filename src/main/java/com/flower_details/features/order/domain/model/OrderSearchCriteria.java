package com.flower_details.features.order.domain.model;

import com.flower_details.shared.domain.DomainException;
import com.flower_details.shared.domain.pagination.SortDirection;

import java.time.LocalDate;

public record OrderSearchCriteria(
		String query,
		Long customerId,
		Long operatorId,
		OrderStatus status,
		FulfillmentType fulfillmentType,
		LocalDate createdFrom,
		LocalDate createdTo,
		OrderSortField sortField,
		SortDirection sortDirection
) {

	public OrderSearchCriteria {
		query = normalizeQuery(query);
		if (customerId != null && customerId < 1) throw new DomainException("El cliente debe ser valido");
		if (operatorId != null && operatorId < 1) throw new DomainException("El operador debe ser valido");
		if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
			throw new DomainException("La fecha inicial no puede ser posterior a la fecha final");
		}
		sortField = sortField == null ? OrderSortField.CREATED_AT : sortField;
		sortDirection = sortDirection == null ? SortDirection.DESC : sortDirection;
	}

	private static String normalizeQuery(String value) {
		if (value == null || value.isBlank()) return null;
		String normalized = value.trim();
		if (normalized.length() > 120) throw new DomainException("La busqueda de pedidos supera los 120 caracteres");
		return normalized;
	}
}
