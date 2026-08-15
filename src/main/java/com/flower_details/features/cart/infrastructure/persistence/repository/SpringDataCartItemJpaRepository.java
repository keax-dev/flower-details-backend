package com.flower_details.features.cart.infrastructure.persistence.repository;

import com.flower_details.features.cart.infrastructure.persistence.entity.CartItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataCartItemJpaRepository extends JpaRepository<CartItemJpaEntity, Long> {

	List<CartItemJpaEntity> findAllByCart_IdOrderByCreatedAtAsc(Long cartId);

	Optional<CartItemJpaEntity> findByCart_IdAndProduct_Id(Long cartId, Long productId);

	Optional<CartItemJpaEntity> findByIdAndCart_Id(Long id, Long cartId);
}
