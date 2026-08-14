package com.flower_details.features.users.infrastructure.persistence.repository;

import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.features.users.infrastructure.persistence.entity.UserJpaEntity;
import com.flower_details.features.users.infrastructure.persistence.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaUserRepository implements UserRepository {

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
	public PageResult<User> findAll(PageRequest pageRequest) {
		Page<UserJpaEntity> page = repository.findAll(
				org.springframework.data.domain.PageRequest.of(
						pageRequest.page(), pageRequest.size(), Sort.by(Sort.Direction.DESC, "createdAt")
				)
		);
		return new PageResult<>(
				page.getContent().stream().map(UserPersistenceMapper::toDomain).toList(),
				page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
		);
	}

	private static String normalizeEmail(String email) {
		if (email == null) {
			return "";
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
