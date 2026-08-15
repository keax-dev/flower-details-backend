package com.flower_details.features.order.infrastructure.persistence.mapper;

import com.flower_details.features.order.domain.model.OrderAudit;
import com.flower_details.features.order.infrastructure.persistence.entity.OrderAuditJpaEntity;
import com.flower_details.features.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.flower_details.features.users.infrastructure.persistence.entity.UserJpaEntity;

public final class OrderAuditPersistenceMapper {

	private OrderAuditPersistenceMapper() {
	}

	public static OrderAuditJpaEntity toEntity(OrderAudit audit, OrderJpaEntity order, UserJpaEntity actor) {
		return new OrderAuditJpaEntity(
				audit.id(), order, actor, audit.action(), audit.previousStatus(), audit.currentStatus(), audit.details(), audit.createdAt()
		);
	}

	public static OrderAudit toDomain(OrderAuditJpaEntity entity) {
		return OrderAudit.restore(
				entity.getId(), entity.getOrderId(), entity.getActorUserId(), entity.getAction(), entity.getPreviousStatus(),
				entity.getCurrentStatus(), entity.getDetails(), entity.getCreatedAt()
		);
	}
}
