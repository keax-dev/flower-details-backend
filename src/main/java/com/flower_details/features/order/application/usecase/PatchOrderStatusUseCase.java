package com.flower_details.features.order.application.usecase;

import com.flower_details.features.order.application.dto.view.OrderView;
import com.flower_details.features.order.application.exception.OrderNotFoundException;
import com.flower_details.features.order.domain.model.Order;
import com.flower_details.features.order.domain.model.OrderAudit;
import com.flower_details.features.order.domain.model.OrderAuditAction;
import com.flower_details.features.order.domain.model.OrderItem;
import com.flower_details.features.order.domain.model.OrderStatus;
import com.flower_details.features.order.domain.repository.OrderAuditRepository;
import com.flower_details.features.order.domain.repository.OrderItemRepository;
import com.flower_details.features.order.domain.repository.OrderRepository;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatchOrderStatusUseCase {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final OrderAuditRepository orderAuditRepository;
	private final Clock clock;

	@Transactional
	public OrderView execute(Long id, Long requesterId, UserRole role, OrderStatus status) {
		Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
		if (role == UserRole.OPERATOR && !requesterId.equals(order.assignedOperatorId())) {
			throw new DomainException("El operador solo puede gestionar pedidos que tiene asignados");
		}
		OrderStatus previousStatus = order.status();
		order.changeStatus(status, clock.instant());
		Order savedOrder = orderRepository.save(order);
		orderAuditRepository.save(OrderAudit.create(
				savedOrder.id(), requesterId, OrderAuditAction.STATUS_CHANGED, previousStatus,
				savedOrder.status(), null, clock.instant()
		));
		return OrderView.from(savedOrder, orderItemRepository.findByOrderIds(List.of(savedOrder.id())));
	}
}
