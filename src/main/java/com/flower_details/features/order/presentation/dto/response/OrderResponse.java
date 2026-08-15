package com.flower_details.features.order.presentation.dto.response;

import com.flower_details.features.order.application.dto.view.OrderView;
import com.flower_details.features.order.domain.model.FulfillmentType;
import com.flower_details.features.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
		Long id,
		String orderNumber,
		Long customerId,
		Long assignedOperatorId,
		OrderStatus status,
		FulfillmentType fulfillmentType,
		BigDecimal total,
		String contactName,
		String contactPhone,
		String deliveryAddress,
		String additionalInstructions,
		String cancellationReason,
		Instant createdAt,
		Instant assignedAt,
		Instant preparationStartedAt,
		Instant readyAt,
		Instant dispatchedAt,
		Instant deliveredAt,
		Instant cancelledAt,
		List<OrderItemResponse> items
) {

	public static OrderResponse from(OrderView view) {
		return new OrderResponse(
				view.id(),
				view.orderNumber(),
				view.customerId(),
				view.assignedOperatorId(),
				view.status(),
				view.fulfillmentType(),
				view.total(),
				view.contactName(),
				view.contactPhone(),
				view.deliveryAddress(),
				view.additionalInstructions(),
				view.cancellationReason(),
				view.createdAt(),
				view.assignedAt(),
				view.preparationStartedAt(),
				view.readyAt(),
				view.dispatchedAt(),
				view.deliveredAt(),
				view.cancelledAt(),
				view.items().stream().map(OrderItemResponse::from).toList()
		);
	}
}
