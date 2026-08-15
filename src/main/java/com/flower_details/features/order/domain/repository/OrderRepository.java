package com.flower_details.features.order.domain.repository;

import com.flower_details.features.order.domain.model.Order;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;

import java.util.Optional;

public interface OrderRepository {
	Order save(Order order);
	Optional<Order> findById(Long id);
	PageResult<Order> findByCustomerId(Long customerId, PageRequest pageRequest);
	PageResult<Order> findAll(PageRequest pageRequest);
}
