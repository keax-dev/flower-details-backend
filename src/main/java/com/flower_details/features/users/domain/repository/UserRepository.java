package com.flower_details.features.users.domain.repository;

import com.flower_details.features.users.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

	User save(User user);

	void delete(User user);

	Optional<User> findById(Long id);

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	List<User> findAll();
}
