package com.flower_details.features.cart.infrastructure.persistence.mapper;

import com.flower_details.features.cart.domain.model.CartItem;
import com.flower_details.features.cart.infrastructure.persistence.entity.CartItemJpaEntity;
import com.flower_details.features.cart.infrastructure.persistence.entity.CartJpaEntity;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductJpaEntity;

public final class CartItemPersistenceMapper {

	private CartItemPersistenceMapper() {
	}

	public static CartItemJpaEntity toEntity(
			CartItem item, CartJpaEntity cart, ProductJpaEntity product
	) {
		return new CartItemJpaEntity(
				item.id(), cart, product, item.quantity(), item.unitPrice(), item.createdAt(), item.updatedAt()
		);
	}

	public static CartItem toDomain(CartItemJpaEntity entity) {
		return CartItem.restore(
				entity.getId(), entity.getCartId(), entity.getProductId(), entity.getQuantity(), entity.getUnitPrice(),
				entity.getCreatedAt(), entity.getUpdatedAt()
		);
	}
}
