package com.flower_details.features.auth.application.security;

import com.flower_details.features.users.domain.model.UserRole;

public interface AuthenticatedUser {

	Long id();

	String email();

	UserRole role();
}
