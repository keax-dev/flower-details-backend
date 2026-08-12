package com.flower_details.features.auth.infrastructure.security;

import com.flower_details.features.users.domain.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class AuthenticatedUserPrincipal implements UserDetails {

	private final Long id;
	private final String email;
	private final UserRole role;

	public Long id() {
		return id;
	}

	public String email() {
		return email;
	}

	public UserRole role() {
		return role;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public String getUsername() {
		return email;
	}
}
