package com.flower_details.features.order.infrastructure.persistence.repository;

import com.flower_details.features.order.domain.model.OrderAudit;
import com.flower_details.features.order.domain.repository.OrderAuditRepository;
import com.flower_details.features.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.flower_details.features.order.infrastructure.persistence.mapper.OrderAuditPersistenceMapper;
import com.flower_details.features.users.infrastructure.persistence.entity.UserJpaEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
class JpaOrderAuditRepository implements OrderAuditRepository {

	private final SpringDataOrderAuditJpaRepository repository;
	private final EntityManager entityManager;

	@Override
	public OrderAudit save(OrderAudit audit) {
		OrderJpaEntity order = entityManager.getReference(OrderJpaEntity.class, audit.orderId());
		UserJpaEntity actor = entityManager.getReference(UserJpaEntity.class, audit.actorUserId());
		return OrderAuditPersistenceMapper.toDomain(repository.save(OrderAuditPersistenceMapper.toEntity(audit, order, actor)));
	}

	@Override
	public List<OrderAudit> findByOrderId(Long orderId) {
		return repository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(orderId).stream()
				.map(OrderAuditPersistenceMapper::toDomain)
				.toList();
	}
}
