package com.flower_details.shared.infrastructure.security;

import com.flower_details.shared.security.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BCryptPasswordHasher implements PasswordHasher {

	private final PasswordEncoder passwordEncoder;

	BCryptPasswordHasher(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public String hash(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(String rawPassword, String passwordHash) {
		return passwordEncoder.matches(rawPassword, passwordHash);
	}
}
