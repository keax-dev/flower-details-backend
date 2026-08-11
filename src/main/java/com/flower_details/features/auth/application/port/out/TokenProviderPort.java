package com.flower_details.features.auth.application.port.out;

import com.flower_details.features.users.domain.model.User;

import java.util.Optional;

public interface TokenProviderPort {

	String generate(User user);

	Optional<TokenClaims> validate(String token);

	long expirationSeconds();
}
