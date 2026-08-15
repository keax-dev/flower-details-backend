package com.flower_details.features.order.domain.repository;

import com.flower_details.features.order.domain.model.OrderItem;

import java.util.Collection;
import java.util.List;

public interface OrderItemRepository {
	List<OrderItem> saveAll(List<OrderItem> items);
	List<OrderItem> findByOrderIds(Collection<Long> orderIds);
}
