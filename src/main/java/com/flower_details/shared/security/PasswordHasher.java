package com.flower_details.shared.security;

public interface PasswordHasher {

	String hash(String rawPassword);

	boolean matches(String rawPassword, String passwordHash);
}
