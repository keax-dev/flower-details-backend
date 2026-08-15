package com.flower_details.features.cart.domain.repository;

import com.flower_details.features.cart.domain.model.Cart;

import java.util.Optional;

public interface CartRepository {

	Cart save(Cart cart);

	Optional<Cart> findActiveByCustomerId(Long customerId);

	Optional<Cart> findActiveByCustomerIdForUpdate(Long customerId);
}
