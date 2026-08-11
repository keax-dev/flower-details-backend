package com.flower_details.features.auth.infrastructure.security;

import com.flower_details.features.users.application.port.out.UserRepositoryPort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class DatabaseUserDetailsService implements UserDetailsService {

	private final UserRepositoryPort userRepository;

	DatabaseUserDetailsService(UserRepositoryPort userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		return userRepository.findByEmail(username)
				.map(user -> new org.springframework.security.core.userdetails.User(
						user.email(),
						user.passwordHash(),
						user.active(),
						true,
						true,
						true,
						List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()))
				))
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
	}
}
