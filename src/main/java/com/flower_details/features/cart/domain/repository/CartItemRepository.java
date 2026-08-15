package com.flower_details.features.cart.domain.repository;

import com.flower_details.features.cart.domain.model.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository {

	CartItem save(CartItem item);

	List<CartItem> findActiveByCartId(Long cartId);

	Optional<CartItem> findActiveByCartIdAndProductId(Long cartId, Long productId);

	Optional<CartItem> findActiveByIdAndCartId(Long itemId, Long cartId);

	void delete(CartItem item);

	void deleteAllActiveByCartId(Long cartId);
}
