package com.flower_details.features.order.infrastructure.persistence.repository;
import com.flower_details.features.order.domain.model.Order;
import com.flower_details.features.order.domain.repository.OrderRepository;
import com.flower_details.features.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.flower_details.features.order.infrastructure.persistence.mapper.OrderPersistenceMapper;
import com.flower_details.features.users.infrastructure.persistence.entity.UserJpaEntity;
import jakarta.persistence.EntityManager;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaOrderRepository implements OrderRepository {

	private final SpringDataOrderJpaRepository repository;
	private final EntityManager entityManager;

	@Override
	public Order save(Order order) {
		UserJpaEntity customer = entityManager.getReference(UserJpaEntity.class, order.customerId());
		UserJpaEntity assignedOperator = order.assignedOperatorId() == null
				? null
				: entityManager.getReference(UserJpaEntity.class, order.assignedOperatorId());
		return OrderPersistenceMapper.toDomain(
				repository.save(OrderPersistenceMapper.toEntity(order, customer, assignedOperator))
		);
	}

	@Override
	public Optional<Order> findById(Long id) {
		return repository.findById(id).map(OrderPersistenceMapper::toDomain);
	}

	@Override
	public PageResult<Order> findByCustomerId(Long customerId, PageRequest pageRequest) {
		return toPage(repository.findAllByCustomer_Id(customerId, pageable(pageRequest)));
	}

	@Override
	public PageResult<Order> findAll(PageRequest pageRequest) {
		return toPage(repository.findAll(pageable(pageRequest)));
	}

	private Pageable pageable(PageRequest pageRequest) {
		return org.springframework.data.domain.PageRequest.of(
				pageRequest.page(), pageRequest.size(), Sort.by(Sort.Direction.DESC, "createdAt")
		);
	}

	private PageResult<Order> toPage(Page<OrderJpaEntity> page) {
		return new PageResult<>(
				page.getContent().stream().map(OrderPersistenceMapper::toDomain).toList(),
				page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
		);
	}
}
