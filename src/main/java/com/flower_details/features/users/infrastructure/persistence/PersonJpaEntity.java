package com.flower_details.features.users.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.time.Instant;

@Entity
@SoftDelete(columnName = "deleted_at", strategy = SoftDeleteType.TIMESTAMP)
@Table(
		name = "people",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_people_user", columnNames = "user_id"),
				@UniqueConstraint(name = "uk_people_document_number", columnNames = "document_number")
		}
)
class PersonJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "user_id",
			nullable = false,
			unique = true,
			foreignKey = @ForeignKey(name = "fk_people_user")
	)
	private UserJpaEntity user;

	@Column(nullable = false, length = 80)
	private String names;

	@Column(nullable = false, length = 80)
	private String lastnames;

	@Column(length = 30)
	private String phone;

	@Column(name = "document_number", length = 30)
	private String documentNumber;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected PersonJpaEntity() {
	}

	PersonJpaEntity(
			Long id,
			UserJpaEntity user,
			String names,
			String lastnames,
			String phone,
			String documentNumber,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = id;
		this.user = user;
		this.names = names;
		this.lastnames = lastnames;
		this.phone = phone;
		this.documentNumber = documentNumber;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

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

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return user.getId();
	}

	public String getNames() {
		return names;
	}

	public String getLastnames() {
		return lastnames;
	}

	public String getPhone() {
		return phone;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
