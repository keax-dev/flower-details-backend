package com.flower_details.features.order.infrastructure.persistence.repository;
import com.flower_details.features.order.infrastructure.persistence.entity.OrderJpaEntity;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
interface SpringDataOrderJpaRepository extends JpaRepository<OrderJpaEntity, Long>, JpaSpecificationExecutor<OrderJpaEntity> {
	Optional<OrderJpaEntity> findById(Long id);
}
