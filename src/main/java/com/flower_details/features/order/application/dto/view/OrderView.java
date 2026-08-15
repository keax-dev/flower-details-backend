package com.flower_details.features.order.application.dto.view;

import com.flower_details.features.order.domain.model.FulfillmentType;
import com.flower_details.features.order.domain.model.Order;
import com.flower_details.features.order.domain.model.OrderItem;
import com.flower_details.features.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderView(
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
		List<OrderItemView> items
) {

	public static OrderView from(Order order, List<OrderItem> items) {
		return new OrderView(
				order.id(),
				order.orderNumber(),
				order.customerId(),
				order.assignedOperatorId(),
				order.status(),
				order.fulfillmentType(),
				order.total(),
				order.contactName(),
				order.contactPhone(),
				order.deliveryAddress(),
				order.additionalInstructions(),
				order.cancellationReason(),
				order.createdAt(),
				order.assignedAt(),
				order.preparationStartedAt(),
				order.readyAt(),
				order.dispatchedAt(),
				order.deliveredAt(),
				order.cancelledAt(),
				items.stream().map(OrderItemView::from).toList()
		);
	}
}
