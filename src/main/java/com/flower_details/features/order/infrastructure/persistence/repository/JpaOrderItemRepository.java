package com.flower_details.features.order.infrastructure.persistence.repository;
import com.flower_details.features.order.domain.model.OrderItem;
import com.flower_details.features.order.domain.repository.OrderItemRepository;
import com.flower_details.features.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.flower_details.features.order.infrastructure.persistence.mapper.OrderItemPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
class JpaOrderItemRepository implements OrderItemRepository {

	private final SpringDataOrderItemJpaRepository repository;

	@Override
	public List<OrderItem> saveAll(List<OrderItem> items) {
		return repository.saveAll(items.stream().map(OrderItemPersistenceMapper::toEntity).toList()).stream()
				.map(OrderItemPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public List<OrderItem> findByOrderIds(Collection<Long> orderIds) {
		if (orderIds.isEmpty()) {
			return List.of();
		}
		return repository.findAllByOrderIdInOrderByCreatedAtAsc(orderIds).stream()
				.map(OrderItemPersistenceMapper::toDomain)
				.toList();
	}
}
