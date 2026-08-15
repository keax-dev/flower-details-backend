package com.flower_details.features.order.domain.repository;

import com.flower_details.features.order.domain.model.OrderAudit;

import java.util.List;

public interface OrderAuditRepository {
	OrderAudit save(OrderAudit audit);
	List<OrderAudit> findByOrderId(Long orderId);
}
