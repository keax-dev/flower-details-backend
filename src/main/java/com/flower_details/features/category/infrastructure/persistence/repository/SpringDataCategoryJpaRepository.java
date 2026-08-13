package com.flower_details.features.category.infrastructure.persistence.repository;

import com.flower_details.features.category.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

interface SpringDataCategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

	Optional<CategoryJpaEntity> findById(Long id);

	boolean existsByTitleIgnoreCase(String title);

	boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);

	List<CategoryJpaEntity> findByIdIn(Collection<Long> ids);

	List<CategoryJpaEntity> findAllByActiveTrueOrderByTitleAsc();
}
