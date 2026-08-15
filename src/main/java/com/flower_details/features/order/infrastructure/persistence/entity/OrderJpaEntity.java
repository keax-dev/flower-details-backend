package com.flower_details.features.order.infrastructure.persistence.entity;

import com.flower_details.features.order.domain.model.FulfillmentType;
import com.flower_details.features.order.domain.model.OrderStatus;
import com.flower_details.features.users.infrastructure.persistence.entity.UserJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@SoftDelete(columnName = "deleted_at", strategy = SoftDeleteType.TIMESTAMP)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "orders")
public class OrderJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_number", nullable = false, length = 40)
	private String orderNumber;

	@ManyToOne(optional = false)
	@JoinColumn(
			name = "customer_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_orders_customer")
	)
	private UserJpaEntity customer;

	@ManyToOne
	@JoinColumn(
			name = "assigned_operator_id",
			foreignKey = @ForeignKey(name = "fk_orders_assigned_operator")
	)
	private UserJpaEntity assignedOperator;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private OrderStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "fulfillment_type", nullable = false, length = 20)
	private FulfillmentType fulfillmentType;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	@Column(name = "contact_name", nullable = false, length = 160)
	private String contactName;

	@Column(name = "contact_phone", nullable = false, length = 30)
	private String contactPhone;

	@Column(name = "delivery_address", length = 500)
	private String deliveryAddress;

	@Column(name = "additional_instructions", length = 1_000)
	private String additionalInstructions;

	@Column(name = "cancellation_reason", length = 500)
	private String cancellationReason;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "assigned_at")
	private Instant assignedAt;

	@Column(name = "preparation_started_at")
	private Instant preparationStartedAt;

	@Column(name = "ready_at")
	private Instant readyAt;

	@Column(name = "dispatched_at")
	private Instant dispatchedAt;

	@Column(name = "delivered_at")
	private Instant deliveredAt;

	@Column(name = "cancelled_at")
	private Instant cancelledAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		if (createdAt == null) {
			createdAt = now;
		}
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}

	public Long getCustomerId() {
		return customer.getId();
	}

	public Long getAssignedOperatorId() {
		return assignedOperator == null ? null : assignedOperator.getId();
	}
}
