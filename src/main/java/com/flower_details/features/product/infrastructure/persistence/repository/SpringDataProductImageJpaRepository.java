package com.flower_details.features.product.infrastructure.persistence.repository;

import com.flower_details.features.product.infrastructure.persistence.entity.ProductImageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

interface SpringDataProductImageJpaRepository extends JpaRepository<ProductImageJpaEntity, Long> {

	List<ProductImageJpaEntity> findAllByProduct_IdAndActiveTrueOrderBySortOrderAsc(Long productId);

	@Query("""
			select image
			from ProductImageJpaEntity image
			where image.product.id in :productIds
			  and image.active = true
			order by image.product.id asc, image.sortOrder asc
			""")
	List<ProductImageJpaEntity> findActiveByProductIds(@Param("productIds") Collection<Long> productIds);

	Optional<ProductImageJpaEntity> findByStoredFileNameAndActiveTrue(String storedFileName);
}
