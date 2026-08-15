package com.flower_details.features.cart.presentation.dto.response;

import com.flower_details.features.cart.application.dto.view.CartItemView;

import java.math.BigDecimal;

public record CartItemResponse(Long id, CartProductResponse product, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
	public static CartItemResponse from(CartItemView view) {
		return new CartItemResponse(view.id(), CartProductResponse.from(view.product()), view.quantity(), view.unitPrice(), view.subtotal());
	}
}
