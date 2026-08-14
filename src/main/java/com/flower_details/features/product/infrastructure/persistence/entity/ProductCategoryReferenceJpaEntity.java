package com.flower_details.features.product.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

@Entity
@Immutable
@SoftDelete(columnName = "deleted_at", strategy = SoftDeleteType.TIMESTAMP)
@Table(name = "categories")
public class ProductCategoryReferenceJpaEntity {

	@Id
	private Long id;

	@Column(nullable = false)
	private boolean active;

	Long getId() {
		return id;
	}

	boolean isActive() {
		return active;
	}
}
