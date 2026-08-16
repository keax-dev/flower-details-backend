package com.flower_details.features.cart.application.dto.view;

import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.model.ProductImage;

public record CartProductView(Long id, String title, String imageUrl, boolean available) {

	public static CartProductView from(Product product, ProductImage primaryImage) {
		return new CartProductView(product.id(), product.title(), primaryImage == null ? null : primaryImage.url(), true);
	}

	public static CartProductView unavailable(Long productId) {
		return new CartProductView(productId, "Producto no disponible", null, false);
	}
}
