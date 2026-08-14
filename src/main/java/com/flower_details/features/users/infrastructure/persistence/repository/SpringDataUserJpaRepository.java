package com.flower_details.features.users.infrastructure.persistence.repository;

import com.flower_details.features.users.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataUserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

	Optional<UserJpaEntity> findById(Long id);

	Optional<UserJpaEntity> findByEmail(String email);

	boolean existsByEmail(String email);

	List<UserJpaEntity> findAllByOrderByCreatedAtDesc();
}
