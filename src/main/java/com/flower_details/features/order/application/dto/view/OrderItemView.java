package com.flower_details.features.order.application.dto.view;

import com.flower_details.features.order.domain.model.OrderItem;

import java.math.BigDecimal;

public record OrderItemView(
		Long id,
		Long productId,
		String productTitle,
		String productImageUrl,
		int quantity,
		BigDecimal unitPrice,
		BigDecimal subtotal
) {

	public static OrderItemView from(OrderItem item) {
		return new OrderItemView(
				item.id(),
				item.productId(),
				item.productTitle(),
				item.productImageUrl(),
				item.quantity(),
				item.unitPrice(),
				item.subtotal()
		);
	}
}
