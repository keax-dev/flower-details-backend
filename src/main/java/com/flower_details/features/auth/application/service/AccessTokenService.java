package com.flower_details.features.auth.application.service;

import com.flower_details.features.users.domain.model.User;

public interface AccessTokenService {

	String generate(User user);

	long expirationSeconds();
}
