package com.flower_details.features.order.infrastructure.persistence.mapper;

import com.flower_details.features.order.domain.model.Order;
import com.flower_details.features.order.infrastructure.persistence.entity.OrderJpaEntity;

public final class OrderPersistenceMapper {

	private OrderPersistenceMapper() {
	}

	public static OrderJpaEntity toEntity(Order order) {
		return new OrderJpaEntity(
				order.id(),
				order.orderNumber(),
				order.customerId(),
				order.assignedOperatorId(),
				order.status(),
				order.fulfillmentType(),
				order.total(),
				order.contactName(),
				order.contactPhone(),
				order.deliveryAddress(),
				order.additionalInstructions(),
				order.cancellationReason(),
				order.createdAt(),
				order.assignedAt(),
				order.preparationStartedAt(),
				order.readyAt(),
				order.dispatchedAt(),
				order.deliveredAt(),
				order.cancelledAt(),
				order.updatedAt()
		);
	}

	public static Order toDomain(OrderJpaEntity entity) {
		return Order.restore(
				entity.getId(),
				entity.getOrderNumber(),
				entity.getCustomerId(),
				entity.getAssignedOperatorId(),
				entity.getStatus(),
				entity.getFulfillmentType(),
				entity.getTotal(),
				entity.getContactName(),
				entity.getContactPhone(),
				entity.getDeliveryAddress(),
				entity.getAdditionalInstructions(),
				entity.getCancellationReason(),
				entity.getCreatedAt(),
				entity.getAssignedAt(),
				entity.getPreparationStartedAt(),
				entity.getReadyAt(),
				entity.getDispatchedAt(),
				entity.getDeliveredAt(),
				entity.getCancelledAt(),
				entity.getUpdatedAt()
		);
	}
}
