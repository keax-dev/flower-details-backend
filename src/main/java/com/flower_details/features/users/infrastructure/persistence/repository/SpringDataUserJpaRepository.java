package com.flower_details.features.users.infrastructure.persistence.repository;

import com.flower_details.features.users.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

interface SpringDataUserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

	Optional<UserJpaEntity> findById(Long id);

	Optional<UserJpaEntity> findByEmail(String email);

	boolean existsByEmail(String email);

	Page<UserJpaEntity> findAll(Pageable pageable);
}
