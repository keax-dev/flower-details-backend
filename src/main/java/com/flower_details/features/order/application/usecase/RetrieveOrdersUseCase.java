package com.flower_details.features.order.application.usecase;

import com.flower_details.features.order.application.dto.query.OrderSearchQuery;
import com.flower_details.features.order.application.dto.view.OrderAuditView;
import com.flower_details.features.order.application.dto.view.OrderView;
import com.flower_details.features.order.application.exception.OrderNotFoundException;
import com.flower_details.features.order.domain.model.Order;
import com.flower_details.features.order.domain.model.OrderItem;
import com.flower_details.features.order.domain.repository.OrderAuditRepository;
import com.flower_details.features.order.domain.repository.OrderItemRepository;
import com.flower_details.features.order.domain.repository.OrderRepository;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RetrieveOrdersUseCase {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final OrderAuditRepository orderAuditRepository;

	@Transactional(readOnly = true)
	public PageResult<OrderView> mine(Long customerId, OrderSearchQuery query, PageRequest pageRequest) {
		return toViews(orderRepository.search(query.toCriteriaForCustomer(customerId), pageRequest));
	}

	@Transactional(readOnly = true)
	public PageResult<OrderView> all(OrderSearchQuery query, PageRequest pageRequest) {
		return toViews(orderRepository.search(query.toCriteria(), pageRequest));
	}

	@Transactional(readOnly = true)
	public OrderView byId(Long id, Long requesterId, UserRole role) {
		Order order = findOrder(id);
		ensureCanView(order, requesterId, role);
		return OrderView.from(order, orderItemRepository.findByOrderIds(List.of(order.id())));
	}

	@Transactional(readOnly = true)
	public List<OrderAuditView> auditTrail(Long id, Long requesterId, UserRole role) {
		Order order = findOrder(id);
		ensureCanView(order, requesterId, role);
		return orderAuditRepository.findByOrderId(order.id()).stream().map(OrderAuditView::from).toList();
	}

	private Order findOrder(Long id) {
		return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
	}

	private void ensureCanView(Order order, Long requesterId, UserRole role) {
		if (role == UserRole.CUSTOMER && !order.customerId().equals(requesterId)) {
			throw new OrderNotFoundException(order.id());
		}
	}

	private PageResult<OrderView> toViews(PageResult<Order> page) {
		Map<Long, List<OrderItem>> itemsByOrderId = orderItemRepository.findByOrderIds(
				page.items().stream().map(Order::id).toList()
		).stream().collect(Collectors.groupingBy(OrderItem::orderId));
		return page.map(order -> OrderView.from(order, itemsByOrderId.getOrDefault(order.id(), List.of())));
	}
}
