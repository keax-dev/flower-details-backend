package com.flower_details.features.order.domain.model;

import com.flower_details.shared.domain.DomainException;

import java.time.Instant;

public class OrderAudit {

	private final Long id;
	private final Long orderId;
	private final Long actorUserId;
	private final OrderAuditAction action;
	private final OrderStatus previousStatus;
	private final OrderStatus currentStatus;
	private final String details;
	private final Instant createdAt;

	private OrderAudit(
			Long id,
			Long orderId,
			Long actorUserId,
			OrderAuditAction action,
			OrderStatus previousStatus,
			OrderStatus currentStatus,
			String details,
			Instant createdAt
	) {
		if (orderId == null) throw new DomainException("El pedido de auditoria es obligatorio");
		if (actorUserId == null) throw new DomainException("El actor de auditoria es obligatorio");
		if (action == null) throw new DomainException("La accion de auditoria es obligatoria");
		if (currentStatus == null) throw new DomainException("El estado resultante es obligatorio");

		this.id = id;
		this.orderId = orderId;
		this.actorUserId = actorUserId;
		this.action = action;
		this.previousStatus = previousStatus;
		this.currentStatus = currentStatus;
		this.details = normalizeDetails(details);
		this.createdAt = createdAt;
	}

	public static OrderAudit create(
			Long orderId,
			Long actorUserId,
			OrderAuditAction action,
			OrderStatus previousStatus,
			OrderStatus currentStatus,
			String details,
			Instant createdAt
	) {
		return new OrderAudit(null, orderId, actorUserId, action, previousStatus, currentStatus, details, createdAt);
	}

	public static OrderAudit restore(
			Long id,
			Long orderId,
			Long actorUserId,
			OrderAuditAction action,
			OrderStatus previousStatus,
			OrderStatus currentStatus,
			String details,
			Instant createdAt
	) {
		return new OrderAudit(id, orderId, actorUserId, action, previousStatus, currentStatus, details, createdAt);
	}

	public Long id() { return id; }
	public Long orderId() { return orderId; }
	public Long actorUserId() { return actorUserId; }
	public OrderAuditAction action() { return action; }
	public OrderStatus previousStatus() { return previousStatus; }
	public OrderStatus currentStatus() { return currentStatus; }
	public String details() { return details; }
	public Instant createdAt() { return createdAt; }

	private static String normalizeDetails(String value) {
		if (value == null || value.isBlank()) return null;
		String normalized = value.trim();
		if (normalized.length() > 500) throw new DomainException("El detalle de auditoria supera los 500 caracteres");
		return normalized;
	}
}
