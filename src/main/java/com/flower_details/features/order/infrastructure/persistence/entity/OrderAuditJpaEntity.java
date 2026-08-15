package com.flower_details.features.order.infrastructure.persistence.entity;

import com.flower_details.features.order.domain.model.OrderAuditAction;
import com.flower_details.features.order.domain.model.OrderStatus;
import com.flower_details.features.users.infrastructure.persistence.entity.UserJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.time.Instant;

@Entity
@SoftDelete(columnName = "deleted_at", strategy = SoftDeleteType.TIMESTAMP)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "order_audits")
public class OrderAuditJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_audits_order"))
	private OrderJpaEntity order;

	@ManyToOne(optional = false)
	@JoinColumn(name = "actor_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_audits_actor"))
	private UserJpaEntity actor;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private OrderAuditAction action;

	@Enumerated(EnumType.STRING)
	@Column(name = "previous_status", length = 30)
	private OrderStatus previousStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "current_status", nullable = false, length = 30)
	private OrderStatus currentStatus;

	@Column(length = 500)
	private String details;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public Long getOrderId() {
		return order.getId();
	}

	public Long getActorUserId() {
		return actor.getId();
	}
}
