package com.flower_details.features.cart.infrastructure.persistence.repository;

import com.flower_details.features.cart.domain.model.CartStatus;
import com.flower_details.features.cart.infrastructure.persistence.entity.CartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataCartJpaRepository extends JpaRepository<CartJpaEntity, Long> {

	Optional<CartJpaEntity> findByCustomer_IdAndStatus(Long customerId, CartStatus status);
}
