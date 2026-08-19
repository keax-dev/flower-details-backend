package com.flower_details.features.users.domain.model;

import com.flower_details.shared.domain.DomainException;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public class User {

	private final Long id;
	private String email;
	private String passwordHash;
	private UserRole role;
	private boolean active;
	private final Instant createdAt;
	private Instant updatedAt;

	private User(
			Long id,
			String email,
			String passwordHash,
			UserRole role,
			boolean active,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = id;
		this.email = normalizeEmail(email);
		this.passwordHash = requireText(passwordHash, "La contrasena cifrada es obligatoria");
		this.role = Objects.requireNonNull(role, "El rol es obligatorio");
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static User registerCustomer(String email, String passwordHash) {
		return new User(null, email, passwordHash, UserRole.CUSTOMER, true, null, null);
	}

	public static User createStaff(String email, String passwordHash, UserRole role) {
		if (role == UserRole.CUSTOMER) {
			throw new DomainException("El rol de staff debe ser ADMIN u OPERATOR");
		}
		return new User(null, email, passwordHash, role, true, null, null);
	}

	public static User restore(
			Long id,
			String email,
			String passwordHash,
			UserRole role,
			boolean active,
			Instant createdAt,
			Instant updatedAt
	) {
		return new User(id, email, passwordHash, role, active, createdAt, updatedAt);
	}

	public void deactivate() {
		active = false;
	}

	public void activate() {
		active = true;
	}

	public void updatePasswordHash(String passwordHash) {
		this.passwordHash = requireText(passwordHash, "La contrasena cifrada es obligatoria");
	}

	public Long id() {
		return id;
	}

	public String email() {
		return email;
	}

	public String passwordHash() {
		return passwordHash;
	}

	public UserRole role() {
		return role;
	}

	public boolean active() {
		return active;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}

	private static String normalizeEmail(String value) {
		return requireText(value, "El correo es obligatorio").toLowerCase(Locale.ROOT);
	}

	private static String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new DomainException(message);
		}
		return value.trim();
	}
}
