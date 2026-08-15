package com.flower_details.features.order.infrastructure.persistence.repository;
import com.flower_details.features.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
interface SpringDataOrderItemJpaRepository extends JpaRepository<OrderItemJpaEntity, Long> {
	List<OrderItemJpaEntity> findAllByOrder_IdInOrderByCreatedAtAsc(Collection<Long> orderIds);
}
