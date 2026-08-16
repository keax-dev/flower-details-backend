package com.flower_details.features.order.application.usecase;

import com.flower_details.features.order.application.exception.OrderNotFoundException;
import com.flower_details.features.order.domain.model.Order;
import com.flower_details.features.order.domain.model.OrderAudit;
import com.flower_details.features.order.domain.model.OrderAuditAction;
import com.flower_details.features.order.domain.model.OrderStatus;
import com.flower_details.features.order.domain.repository.OrderAuditRepository;
import com.flower_details.features.order.domain.repository.OrderRepository;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class PatchOrderCancellationUseCase {

	private final OrderRepository orderRepository;
	private final OrderAuditRepository orderAuditRepository;
	private final Clock clock;

	@Transactional
	public void execute(Long id, Long requesterId, UserRole role, String reason) {
		Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
		if (role == UserRole.CUSTOMER
				&& (!order.customerId().equals(requesterId) || order.status() != OrderStatus.GENERATED)) {
			throw new DomainException("El cliente solo puede cancelar sus pedidos generados");
		}
		OrderStatus previousStatus = order.status();
		order.cancel(reason, clock.instant());
		Order savedOrder = orderRepository.save(order);
		orderAuditRepository.save(OrderAudit.create(
				savedOrder.id(), requesterId, OrderAuditAction.CANCELLED, previousStatus,
				savedOrder.status(), reason, clock.instant()
		));
	}
}
