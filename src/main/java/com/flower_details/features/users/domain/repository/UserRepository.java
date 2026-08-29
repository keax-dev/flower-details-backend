package com.flower_details.features.users.domain.repository;

import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;

import java.util.Optional;

public interface UserRepository {

	User save(User user);

	void delete(User user);

	Optional<User> findById(Long id);

	Optional<User> findByIdForUpdate(Long id);

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByEmailForAnotherUser(String email, Long userId);

	PageResult<User> findAll(PageRequest pageRequest);

	PageResult<User> findAllByRole(UserRole role, PageRequest pageRequest);
}
