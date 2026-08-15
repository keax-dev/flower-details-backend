package com.flower_details.features.order.presentation.dto.response;

import com.flower_details.features.order.application.dto.view.OrderAuditView;
import com.flower_details.features.order.domain.model.OrderAuditAction;
import com.flower_details.features.order.domain.model.OrderStatus;

import java.time.Instant;

public record OrderAuditResponse(
		Long id,
		Long actorUserId,
		OrderAuditAction action,
		OrderStatus previousStatus,
		OrderStatus currentStatus,
		String details,
		Instant createdAt
) {
	public static OrderAuditResponse from(OrderAuditView view) {
		return new OrderAuditResponse(
				view.id(),
				view.actorUserId(),
				view.action(),
				view.previousStatus(),
				view.currentStatus(),
				view.details(),
				view.createdAt()
		);
	}
}
