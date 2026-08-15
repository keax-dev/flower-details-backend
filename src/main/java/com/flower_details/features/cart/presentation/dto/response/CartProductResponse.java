package com.flower_details.features.cart.presentation.dto.response;

import com.flower_details.features.cart.application.dto.view.CartProductView;

public record CartProductResponse(Long id, String title, String imageUrl) {
	public static CartProductResponse from(CartProductView view) {
		return new CartProductResponse(view.id(), view.title(), view.imageUrl());
	}
}
