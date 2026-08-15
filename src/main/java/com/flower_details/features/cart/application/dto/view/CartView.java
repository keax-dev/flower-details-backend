package com.flower_details.features.cart.application.dto.view;

import com.flower_details.features.cart.domain.model.Cart;

import java.math.BigDecimal;
import java.util.List;

public record CartView(Long id, List<CartItemView> items, BigDecimal total) {

	public static CartView empty() {
		return new CartView(null, List.of(), BigDecimal.ZERO);
	}

	public static CartView from(Cart cart, List<CartItemView> items) {
		return new CartView(
				cart.id(),
				List.copyOf(items),
				items.stream().map(CartItemView::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
		);
	}
}
