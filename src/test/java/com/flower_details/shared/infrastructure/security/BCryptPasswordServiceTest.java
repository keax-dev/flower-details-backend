package com.flower_details.shared.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordServiceTest {

	@Test
	void identifiesHashesThatNeedToBeUpgradedToTheConfiguredCost() {
		BCryptPasswordService passwordService = new BCryptPasswordService(new BCryptPasswordEncoder(12));
		String legacyHash = new BCryptPasswordEncoder(10).encode("Password123");

		assertThat(passwordService.matches("Password123", legacyHash)).isTrue();
		assertThat(passwordService.needsRehash(legacyHash)).isTrue();
		assertThat(passwordService.needsRehash(passwordService.hash("Password123"))).isFalse();
	}
}
