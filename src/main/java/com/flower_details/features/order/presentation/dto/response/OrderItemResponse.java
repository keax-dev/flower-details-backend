package com.flower_details.features.order.presentation.dto.response;

import com.flower_details.features.order.application.dto.view.OrderItemView;

import java.math.BigDecimal;

public record OrderItemResponse(
		Long id,
		Long productId,
		String productTitle,
		String productImageUrl,
		int quantity,
		BigDecimal unitPrice,
		BigDecimal subtotal
) {

	static OrderItemResponse from(OrderItemView view) {
		return new OrderItemResponse(
				view.id(),
				view.productId(),
				view.productTitle(),
				view.productImageUrl(),
				view.quantity(),
				view.unitPrice(),
				view.subtotal()
		);
	}
}
