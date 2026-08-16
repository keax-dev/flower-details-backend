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
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatchOrderAssignmentUseCase {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final OrderAuditRepository orderAuditRepository;
	private final UserRepository userRepository;
	private final Clock clock;

	@Transactional
	public OrderView execute(Long id, Long requesterId, UserRole role, Long operatorId) {
		Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
		if (role == UserRole.OPERATOR && order.status() != OrderStatus.GENERATED) {
			throw new DomainException("El operador solo puede tomar pedidos generados");
		}
		Long assignedOperatorId = role == UserRole.OPERATOR ? requesterId : requireOperatorId(operatorId);
		User operator = userRepository.findById(assignedOperatorId)
				.orElseThrow(() -> new DomainException("El operador no existe"));
		if (!operator.active() || operator.role() != UserRole.OPERATOR) {
			throw new DomainException("El usuario asignado debe ser un operador activo");
		}
		OrderStatus previousStatus = order.status();
		order.assignTo(assignedOperatorId, clock.instant());
		Order savedOrder = orderRepository.save(order);
		orderAuditRepository.save(OrderAudit.create(
				savedOrder.id(), requesterId, OrderAuditAction.ASSIGNED, previousStatus,
				savedOrder.status(), "Operador asignado: " + assignedOperatorId, clock.instant()
		));
		return OrderView.from(savedOrder, orderItemRepository.findByOrderIds(List.of(savedOrder.id())));
	}

	private Long requireOperatorId(Long operatorId) {
		if (operatorId == null) {
			throw new DomainException("El operador es obligatorio");
		}
		return operatorId;
	}
}
