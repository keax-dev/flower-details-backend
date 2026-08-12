package com.flower_details.features.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataCategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

	Optional<CategoryJpaEntity> findById(Long id);

	boolean existsByTitleIgnoreCase(String title);

	boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);

	List<CategoryJpaEntity> findAllByActiveTrueOrderByTitleAsc();
}
