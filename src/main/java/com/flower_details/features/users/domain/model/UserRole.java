package com.flower_details.features.users.domain.model;

public enum UserRole {
	ADMIN,
	OPERATOR,
	CUSTOMER;

	public boolean isStaff() {
		return this == ADMIN || this == OPERATOR;
	}
}
