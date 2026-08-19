package com.flower_details.shared.infrastructure.security;

import com.flower_details.shared.domain.security.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BCryptPasswordService implements PasswordService {

	private final PasswordEncoder passwordEncoder;

	public String hash(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	public boolean matches(String rawPassword, String passwordHash) {
		return passwordEncoder.matches(rawPassword, passwordHash);
	}

	public boolean needsRehash(String passwordHash) {
		return passwordEncoder.upgradeEncoding(passwordHash);
	}
}
