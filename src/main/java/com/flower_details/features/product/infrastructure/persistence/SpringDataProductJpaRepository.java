package com.flower_details.features.product.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

	Optional<ProductJpaEntity> findById(Long id);

	Optional<ProductJpaEntity> findByIdAndActiveTrueAndCategory_ActiveTrue(Long id);

	List<ProductJpaEntity> findAllByActiveTrueAndCategory_ActiveTrueOrderByCreatedAtDesc();
}
