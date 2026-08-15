package com.flower_details.features.order.application.dto.view;

import com.flower_details.features.order.domain.model.OrderAudit;
import com.flower_details.features.order.domain.model.OrderAuditAction;
import com.flower_details.features.order.domain.model.OrderStatus;

import java.time.Instant;

public record OrderAuditView(
		Long id,
		Long actorUserId,
		OrderAuditAction action,
		OrderStatus previousStatus,
		OrderStatus currentStatus,
		String details,
		Instant createdAt
) {
	public static OrderAuditView from(OrderAudit audit) {
		return new OrderAuditView(
				audit.id(),
				audit.actorUserId(),
				audit.action(),
				audit.previousStatus(),
				audit.currentStatus(),
				audit.details(),
				audit.createdAt()
		);
	}
}
