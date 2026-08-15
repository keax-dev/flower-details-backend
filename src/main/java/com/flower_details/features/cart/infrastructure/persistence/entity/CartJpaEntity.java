package com.flower_details.features.cart.infrastructure.persistence.entity;

import com.flower_details.features.cart.domain.model.CartStatus;
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

import java.time.Instant;

@Entity
@SoftDelete(columnName = "deleted_at", strategy = SoftDeleteType.TIMESTAMP)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "carts")
public class CartJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(
			name = "customer_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_carts_customer")
	)
	private UserJpaEntity customer;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CartStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

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
}
