package com.flower_details.features.order.infrastructure.persistence.mapper;

import com.flower_details.features.order.domain.model.OrderItem;
import com.flower_details.features.order.infrastructure.persistence.entity.OrderItemJpaEntity;

public final class OrderItemPersistenceMapper {

	private OrderItemPersistenceMapper() {
	}

	public static OrderItemJpaEntity toEntity(OrderItem item) {
		return new OrderItemJpaEntity(
				item.id(),
				item.orderId(),
				item.productId(),
				item.productTitle(),
				item.productImageUrl(),
				item.quantity(),
				item.unitPrice(),
				item.subtotal(),
				item.createdAt()
		);
	}

	public static OrderItem toDomain(OrderItemJpaEntity entity) {
		return new OrderItem(
				entity.getId(),
				entity.getOrderId(),
				entity.getProductId(),
				entity.getProductTitle(),
				entity.getProductImageUrl(),
				entity.getQuantity(),
				entity.getUnitPrice(),
				entity.getSubtotal(),
				entity.getCreatedAt()
		);
	}
}
