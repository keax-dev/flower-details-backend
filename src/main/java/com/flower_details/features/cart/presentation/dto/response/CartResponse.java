package com.flower_details.features.cart.presentation.dto.response;

import com.flower_details.features.cart.application.dto.view.CartView;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long id, List<CartItemResponse> items, BigDecimal total) {
	public static CartResponse from(CartView view) {
		return new CartResponse(view.id(), view.items().stream().map(CartItemResponse::from).toList(), view.total());
	}
}
