package com.flower_details.features.cart.application.dto.view;

import com.flower_details.features.cart.domain.model.CartItem;

import java.math.BigDecimal;

public record CartItemView(
		Long id,
		CartProductView product,
		int quantity,
		BigDecimal unitPrice,
		BigDecimal subtotal
) {

	public static CartItemView from(CartItem item, CartProductView product) {
		return new CartItemView(item.id(), product, item.quantity(), item.unitPrice(), item.subtotal());
	}
}
