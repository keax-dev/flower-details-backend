package com.flower_details.features.cart.infrastructure.persistence.mapper;

import com.flower_details.features.cart.domain.model.Cart;
import com.flower_details.features.cart.infrastructure.persistence.entity.CartJpaEntity;

public final class CartPersistenceMapper {

	private CartPersistenceMapper() {
	}

	public static CartJpaEntity toEntity(Cart cart) {
		return new CartJpaEntity(cart.id(), cart.customerId(), cart.status(), cart.createdAt(), cart.updatedAt());
	}

	public static Cart toDomain(CartJpaEntity entity) {
		return Cart.restore(
				entity.getId(), entity.getCustomerId(), entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt()
		);
	}
}
