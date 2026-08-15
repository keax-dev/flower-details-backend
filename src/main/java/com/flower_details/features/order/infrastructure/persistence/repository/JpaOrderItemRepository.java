package com.flower_details.features.order.infrastructure.persistence.repository;
import com.flower_details.features.order.domain.model.OrderItem;
import com.flower_details.features.order.domain.repository.OrderItemRepository;
import com.flower_details.features.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.flower_details.features.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.flower_details.features.order.infrastructure.persistence.mapper.OrderItemPersistenceMapper;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductJpaEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
class JpaOrderItemRepository implements OrderItemRepository {

	private final SpringDataOrderItemJpaRepository repository;
	private final EntityManager entityManager;

	@Override
	public List<OrderItem> saveAll(List<OrderItem> items) {
		return repository.saveAll(items.stream().map(this::toEntity).toList()).stream()
				.map(OrderItemPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public List<OrderItem> findByOrderIds(Collection<Long> orderIds) {
		if (orderIds.isEmpty()) {
			return List.of();
		}
		return repository.findAllByOrder_IdInOrderByCreatedAtAsc(orderIds).stream()
				.map(OrderItemPersistenceMapper::toDomain)
				.toList();
	}

	private OrderItemJpaEntity toEntity(OrderItem item) {
		OrderJpaEntity order = entityManager.getReference(OrderJpaEntity.class, item.orderId());
		ProductJpaEntity product = entityManager.getReference(ProductJpaEntity.class, item.productId());
		return OrderItemPersistenceMapper.toEntity(item, order, product);
	}
}
