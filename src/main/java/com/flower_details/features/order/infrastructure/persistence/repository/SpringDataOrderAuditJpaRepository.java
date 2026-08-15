package com.flower_details.features.order.infrastructure.persistence.repository;

import com.flower_details.features.order.infrastructure.persistence.entity.OrderAuditJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SpringDataOrderAuditJpaRepository extends JpaRepository<OrderAuditJpaEntity, Long> {
	List<OrderAuditJpaEntity> findAllByOrder_IdOrderByCreatedAtAscIdAsc(Long orderId);
}
