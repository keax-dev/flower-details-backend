package com.flower_details.features.order.domain.model;

import com.flower_details.shared.domain.DomainException;

import java.math.BigDecimal;
import java.time.Instant;

public class Order {

	private final Long id;
	private final Long version;
	private final String orderNumber;
	private final Long customerId;
	private Long assignedOperatorId;
	private OrderStatus status;
	private final FulfillmentType fulfillmentType;
	private final BigDecimal total;
	private final String contactName;
	private final String contactPhone;
	private final String deliveryAddress;
	private final String additionalInstructions;
	private String cancellationReason;
	private final Instant createdAt;
	private Instant assignedAt;
	private Instant preparationStartedAt;
	private Instant readyAt;
	private Instant dispatchedAt;
	private Instant deliveredAt;
	private Instant cancelledAt;
	private Instant updatedAt;

	private Order(
			Long id,
			String orderNumber,
			Long customerId,
			Long assignedOperatorId,
			OrderStatus status,
			FulfillmentType fulfillmentType,
			BigDecimal total,
			String contactName,
			String contactPhone,
			String deliveryAddress,
			String additionalInstructions,
			String cancellationReason,
			Instant createdAt,
			Instant assignedAt,
			Instant preparationStartedAt,
			Instant readyAt,
			Instant dispatchedAt,
			Instant deliveredAt,
			Instant cancelledAt,
			Instant updatedAt,
			Long version
	) {
		this.id = id;
		this.version = version;
		this.orderNumber = requireText(orderNumber, "El numero de pedido es obligatorio", 40);
		if (customerId == null) {
			throw new DomainException("El cliente del pedido es obligatorio");
		}
		this.customerId = customerId;
		this.assignedOperatorId = assignedOperatorId;
		this.status = status == null ? OrderStatus.GENERATED : status;
		this.fulfillmentType = fulfillmentType == null ? FulfillmentType.PICKUP : fulfillmentType;
		if (total == null || total.signum() <= 0) {
			throw new DomainException("El total del pedido debe ser mayor a cero");
		}
		this.total = total;
		this.contactName = requireText(contactName, "El nombre de contacto es obligatorio", 160);
		this.contactPhone = requireText(contactPhone, "El telefono de contacto es obligatorio", 30);
		this.deliveryAddress = normalizeOptional(deliveryAddress, 500);
		if (this.fulfillmentType == FulfillmentType.DELIVERY && this.deliveryAddress == null) {
			throw new DomainException("La direccion es obligatoria para entrega a domicilio");
		}
		this.additionalInstructions = normalizeOptional(additionalInstructions, 1_000);
		this.cancellationReason = normalizeOptional(cancellationReason, 500);
		this.createdAt = createdAt;
		this.assignedAt = assignedAt;
		this.preparationStartedAt = preparationStartedAt;
		this.readyAt = readyAt;
		this.dispatchedAt = dispatchedAt;
		this.deliveredAt = deliveredAt;
		this.cancelledAt = cancelledAt;
		this.updatedAt = updatedAt;
	}

	public static Order create(
			String orderNumber,
			Long customerId,
			FulfillmentType fulfillmentType,
			BigDecimal total,
			String contactName,
			String contactPhone,
			String deliveryAddress,
			String additionalInstructions
	) {
		return new Order(
				null,
				orderNumber,
				customerId,
				null,
				OrderStatus.GENERATED,
				fulfillmentType,
				total,
				contactName,
				contactPhone,
				deliveryAddress,
				additionalInstructions,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null
		);
	}

	public static Order restore(
			Long id,
			String orderNumber,
			Long customerId,
			Long assignedOperatorId,
			OrderStatus status,
			FulfillmentType fulfillmentType,
			BigDecimal total,
			String contactName,
			String contactPhone,
			String deliveryAddress,
			String additionalInstructions,
			String cancellationReason,
			Instant createdAt,
			Instant assignedAt,
			Instant preparationStartedAt,
			Instant readyAt,
			Instant dispatchedAt,
			Instant deliveredAt,
			Instant cancelledAt,
			Instant updatedAt,
			Long version
	) {
		return new Order(
				id,
				orderNumber,
				customerId,
				assignedOperatorId,
				status,
				fulfillmentType,
				total,
				contactName,
				contactPhone,
				deliveryAddress,
				additionalInstructions,
				cancellationReason,
				createdAt,
				assignedAt,
				preparationStartedAt,
				readyAt,
				dispatchedAt,
				deliveredAt,
				cancelledAt,
				updatedAt,
				version
		);
	}

	public void assignTo(Long operatorId, Instant now) {
		if (operatorId == null) {
			throw new DomainException("El operador asignado es obligatorio");
		}
		if (status != OrderStatus.GENERATED && status != OrderStatus.ASSIGNED) {
			throw new DomainException("El pedido no puede asignarse en su estado actual");
		}
		assignedOperatorId = operatorId;
		status = OrderStatus.ASSIGNED;
		if (assignedAt == null) {
			assignedAt = now;
		}
	}

	public void changeStatus(OrderStatus target, Instant now) {
		if (target == null
				|| target == OrderStatus.CANCELLED
				|| target == OrderStatus.GENERATED
				|| target == OrderStatus.ASSIGNED) {
			throw new DomainException("El estado solicitado no puede aplicarse manualmente");
		}
		if (assignedOperatorId == null) {
			throw new DomainException("El pedido debe estar asignado antes de avanzar");
		}
		switch (target) {
			case IN_PREPARATION -> transition(OrderStatus.ASSIGNED, target);
			case READY_FOR_DELIVERY -> transition(OrderStatus.IN_PREPARATION, target);
			case ON_THE_WAY -> {
				if (fulfillmentType != FulfillmentType.DELIVERY) {
					throw new DomainException("Solo pedidos con entrega pueden estar en camino");
				}
				transition(OrderStatus.READY_FOR_DELIVERY, target);
			}
			case DELIVERED -> {
				OrderStatus required = fulfillmentType == FulfillmentType.DELIVERY
						? OrderStatus.ON_THE_WAY
						: OrderStatus.READY_FOR_DELIVERY;
				transition(required, target);
			}
			default -> throw new DomainException("Transicion de pedido invalida");
		}
		status = target;
		switch (target) {
			case IN_PREPARATION -> preparationStartedAt = now;
			case READY_FOR_DELIVERY -> readyAt = now;
			case ON_THE_WAY -> dispatchedAt = now;
			case DELIVERED -> deliveredAt = now;
			default -> { }
		}
	}

	public void cancel(String reason, Instant now) {
		if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
			throw new DomainException("El pedido no puede cancelarse en su estado actual");
		}
		cancellationReason = requireText(reason, "El motivo de cancelacion es obligatorio", 500);
		status = OrderStatus.CANCELLED;
		cancelledAt = now;
	}

	private void transition(OrderStatus expected, OrderStatus target) {
		if (status != expected) {
			throw new DomainException("El pedido debe estar en " + expected + " antes de pasar a " + target);
		}
	}

	public Long id() {
		return id;
	}

	public Long version() {
		return version;
	}

	public String orderNumber() {
		return orderNumber;
	}

	public Long customerId() {
		return customerId;
	}

	public Long assignedOperatorId() {
		return assignedOperatorId;
	}

	public OrderStatus status() {
		return status;
	}

	public FulfillmentType fulfillmentType() {
		return fulfillmentType;
	}

	public BigDecimal total() {
		return total;
	}

	public String contactName() {
		return contactName;
	}

	public String contactPhone() {
		return contactPhone;
	}

	public String deliveryAddress() {
		return deliveryAddress;
	}

	public String additionalInstructions() {
		return additionalInstructions;
	}

	public String cancellationReason() {
		return cancellationReason;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant assignedAt() {
		return assignedAt;
	}

	public Instant preparationStartedAt() {
		return preparationStartedAt;
	}

	public Instant readyAt() {
		return readyAt;
	}

	public Instant dispatchedAt() {
		return dispatchedAt;
	}

	public Instant deliveredAt() {
		return deliveredAt;
	}

	public Instant cancelledAt() {
		return cancelledAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}

	private static String requireText(String value, String message, int maxLength) {
		String normalized = normalizeOptional(value, maxLength);
		if (normalized == null) {
			throw new DomainException(message);
		}
		return normalized;
	}

	private static String normalizeOptional(String value, int maxLength) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String normalized = value.trim();
		if (normalized.length() > maxLength) {
			throw new DomainException("El texto supera el maximo de " + maxLength + " caracteres");
		}
		return normalized;
	}
}
