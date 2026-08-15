package com.flower_details.features.order.infrastructure.persistence.entity;

import com.flower_details.features.product.infrastructure.persistence.entity.ProductJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
@Table(name = "order_items")
public class OrderItemJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(
			name = "order_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_order_items_order")
	)
	private OrderJpaEntity order;

	@ManyToOne(optional = false)
	@JoinColumn(
			name = "product_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_order_items_product")
	)
	private ProductJpaEntity product;

	@Column(name = "product_title", nullable = false, length = 160)
	private String productTitle;

	@Column(name = "product_image_url", length = 500)
	private String productImageUrl;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPrice;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal subtotal;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getOrderId() {
		return order.getId();
	}

	public Long getProductId() {
		return product.getId();
	}
}
