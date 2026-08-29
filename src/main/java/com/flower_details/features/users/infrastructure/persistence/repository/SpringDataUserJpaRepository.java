package com.flower_details.features.users.infrastructure.persistence.repository;

import com.flower_details.features.users.infrastructure.persistence.entity.UserJpaEntity;
import com.flower_details.features.users.domain.model.UserRole;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Collection;

interface SpringDataUserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

	Optional<UserJpaEntity> findById(Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<UserJpaEntity> findWithLockById(Long id);

	Optional<UserJpaEntity> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByEmailAndIdNot(String email, Long id);

	Page<UserJpaEntity> findAll(Pageable pageable);

	Page<UserJpaEntity> findAllByRole(UserRole role, Pageable pageable);

	Page<UserJpaEntity> findAllByRoleIn(Collection<UserRole> roles, Pageable pageable);
}
