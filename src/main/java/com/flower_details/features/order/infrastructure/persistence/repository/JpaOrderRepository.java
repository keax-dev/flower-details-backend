package com.flower_details.features.order.infrastructure.persistence.repository;
import com.flower_details.features.order.domain.model.Order;
import com.flower_details.features.order.domain.model.OrderSearchCriteria;
import com.flower_details.features.order.domain.model.OrderSortField;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.ZoneOffset;

import jakarta.persistence.criteria.Predicate;

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
	public PageResult<Order> search(OrderSearchCriteria criteria, PageRequest pageRequest) {
		return toPage(repository.findAll(specification(criteria), pageable(pageRequest, criteria)));
	}

	private Specification<OrderJpaEntity> specification(OrderSearchCriteria criteria) {
		return (root, query, builder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (criteria.customerId() != null) {
				predicates.add(builder.equal(root.get("customer").get("id"), criteria.customerId()));
			}
			if (criteria.operatorId() != null) {
				predicates.add(builder.equal(root.get("assignedOperator").get("id"), criteria.operatorId()));
			}
			if (criteria.status() != null) {
				predicates.add(builder.equal(root.get("status"), criteria.status()));
			}
			if (criteria.fulfillmentType() != null) {
				predicates.add(builder.equal(root.get("fulfillmentType"), criteria.fulfillmentType()));
			}
			if (criteria.createdFrom() != null) {
				predicates.add(builder.greaterThanOrEqualTo(
						root.get("createdAt"), criteria.createdFrom().atStartOfDay().toInstant(ZoneOffset.UTC)
				));
			}
			if (criteria.createdTo() != null) {
				predicates.add(builder.lessThan(
						root.get("createdAt"), criteria.createdTo().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
				));
			}
			if (criteria.query() != null) {
				String pattern = "%" + criteria.query().toLowerCase(Locale.ROOT) + "%";
				predicates.add(builder.or(
						builder.like(builder.lower(root.get("orderNumber")), pattern),
						builder.like(builder.lower(root.get("contactName")), pattern),
						builder.like(builder.lower(root.get("contactPhone")), pattern)
				));
			}
			return builder.and(predicates.toArray(Predicate[]::new));
		};
	}

	private Pageable pageable(PageRequest pageRequest, OrderSearchCriteria criteria) {
		Sort.Direction direction = criteria.sortDirection() == com.flower_details.shared.domain.pagination.SortDirection.ASC
				? Sort.Direction.ASC
				: Sort.Direction.DESC;
		String property = switch (criteria.sortField()) {
			case OrderSortField.CREATED_AT -> "createdAt";
			case OrderSortField.TOTAL -> "total";
			case OrderSortField.STATUS -> "status";
		};
		return org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size(), Sort.by(direction, property));
	}

	private PageResult<Order> toPage(Page<OrderJpaEntity> page) {
		return new PageResult<>(
				page.getContent().stream().map(OrderPersistenceMapper::toDomain).toList(),
				page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
		);
	}
}
