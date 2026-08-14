package com.flower_details.features.users.infrastructure.persistence.repository;

import com.flower_details.features.users.infrastructure.persistence.entity.PersonJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

interface SpringDataPersonJpaRepository extends JpaRepository<PersonJpaEntity, Long> {

	Optional<PersonJpaEntity> findByUser_Id(Long userId);

	List<PersonJpaEntity> findByUser_IdIn(Collection<Long> userIds);
}
