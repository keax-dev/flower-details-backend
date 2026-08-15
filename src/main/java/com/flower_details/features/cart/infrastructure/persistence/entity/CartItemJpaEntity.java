package com.flower_details.features.cart.infrastructure.persistence.entity;

import com.flower_details.features.product.infrastructure.persistence.entity.ProductJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "cart_items")
public class CartItemJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "cart_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_items_cart"))
	private CartJpaEntity cart;

	@ManyToOne(optional = false)
	@JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_items_product"))
	private ProductJpaEntity product;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPrice;

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

	public Long getCartId() {
		return cart.getId();
	}

	public Long getProductId() {
		return product.getId();
	}
}
