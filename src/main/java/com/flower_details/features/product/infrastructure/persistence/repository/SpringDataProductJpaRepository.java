package com.flower_details.features.product.infrastructure.persistence.repository;

import com.flower_details.features.product.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

interface SpringDataProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

	Optional<ProductJpaEntity> findById(Long id);

	Optional<ProductJpaEntity> findByIdAndActiveTrueAndCategory_ActiveTrue(Long id);

	Page<ProductJpaEntity> findAllByActiveTrueAndCategory_ActiveTrue(Pageable pageable);
}
