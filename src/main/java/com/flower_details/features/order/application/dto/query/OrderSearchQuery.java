package com.flower_details.features.order.application.dto.query;

import com.flower_details.features.order.domain.model.FulfillmentType;
import com.flower_details.features.order.domain.model.OrderSearchCriteria;
import com.flower_details.features.order.domain.model.OrderSortField;
import com.flower_details.features.order.domain.model.OrderStatus;
import com.flower_details.shared.domain.DomainException;
import com.flower_details.shared.domain.pagination.SortDirection;

import java.time.LocalDate;
import java.util.Locale;

public record OrderSearchQuery(
		String query,
		Long customerId,
		Long operatorId,
		OrderStatus status,
		FulfillmentType fulfillmentType,
		LocalDate createdFrom,
		LocalDate createdTo,
		String sortBy,
		String direction
) {

	public OrderSearchCriteria toCriteria() {
		return toCriteriaForCustomer(customerId);
	}

	public OrderSearchCriteria toCriteriaForCustomer(Long customerId) {
		return new OrderSearchCriteria(
				query,
				customerId,
				operatorId,
				status,
				fulfillmentType,
				createdFrom,
				createdTo,
				toSortField(sortBy),
				toSortDirection(direction)
		);
	}

	private static OrderSortField toSortField(String value) {
		if (value == null || value.isBlank()) {
			return OrderSortField.CREATED_AT;
		}
		return switch (value.trim().toLowerCase(Locale.ROOT)) {
			case "createdat", "created_at" -> OrderSortField.CREATED_AT;
			case "total" -> OrderSortField.TOTAL;
			case "status" -> OrderSortField.STATUS;
			default -> throw new DomainException("El campo de ordenamiento de pedidos no es valido");
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
