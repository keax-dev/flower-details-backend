package com.flower_details.features.users.infrastructure.persistence;

import com.flower_details.features.users.application.port.out.UserRepositoryPort;
import com.flower_details.features.users.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class UserPersistenceAdapter implements UserRepositoryPort {

	private final SpringDataUserJpaRepository repository;

	@Override
	public User save(User user) {
		UserJpaEntity saved = repository.save(UserPersistenceMapper.toEntity(user));
		return UserPersistenceMapper.toDomain(saved);
	}

	@Override
	public void delete(User user) {
		repository.findById(user.id()).ifPresent(repository::delete);
	}

	@Override
	public Optional<User> findById(Long id) {
		return repository.findById(id).map(UserPersistenceMapper::toDomain);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return repository.findByEmail(normalizeEmail(email)).map(UserPersistenceMapper::toDomain);
	}

	@Override
	public boolean existsByEmail(String email) {
		return repository.existsByEmail(normalizeEmail(email));
	}

	@Override
	public List<User> findAll() {
		return repository.findAllByOrderByCreatedAtDesc()
				.stream()
				.map(UserPersistenceMapper::toDomain)
				.toList();
	}

	private static String normalizeEmail(String email) {
		if (email == null) {
			return "";
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
