package com.flower_details.features.users.domain.model;

import com.flower_details.shared.domain.DomainException;

import java.time.Instant;

public class Person {

	private final Long id;
	private final Long userId;
	private String names;
	private String lastNames;
	private String phone;
	private String documentNumber;
	private final Instant createdAt;
	private Instant updatedAt;

	private Person(
			Long id,
			Long userId,
			String names,
			String lastNames,
			String phone,
			String documentNumber,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = id;
		this.userId = requireId(userId);
		this.names = requireText(names, "Los nombres son obligatorios");
		this.lastNames = requireText(lastNames, "Los apellidos son obligatorios");
		this.phone = normalizeOptional(phone);
		this.documentNumber = normalizeOptional(documentNumber);
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Person create(
			Long userId,
			String names,
			String lastNames,
			String phone,
			String documentNumber
	) {
		return new Person(null, userId, names, lastNames, phone, documentNumber, null, null);
	}

	public static Person restore(
			Long id,
			Long userId,
			String names,
			String lastNames,
			String phone,
			String documentNumber,
			Instant createdAt,
			Instant updatedAt
	) {
		return new Person(id, userId, names, lastNames, phone, documentNumber, createdAt, updatedAt);
	}

	public Long id() {
		return id;
	}

	public Long userId() {
		return userId;
	}

	public String names() {
		return names;
	}

	public String lastNames() {
		return lastNames;
	}

	public String phone() {
		return phone;
	}

	public String documentNumber() {
		return documentNumber;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}

	private static Long requireId(Long value) {
		if (value == null) {
			throw new DomainException("El usuario de la persona es obligatorio");
		}
		return value;
	}

	private static String normalizeOptional(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private static String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new DomainException(message);
		}
		return value.trim();
	}
}
