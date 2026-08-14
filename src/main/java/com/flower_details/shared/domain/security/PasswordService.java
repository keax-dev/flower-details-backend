package com.flower_details.shared.domain.security;

public interface PasswordService {

	String hash(String rawPassword);

	boolean matches(String rawPassword, String passwordHash);
}
