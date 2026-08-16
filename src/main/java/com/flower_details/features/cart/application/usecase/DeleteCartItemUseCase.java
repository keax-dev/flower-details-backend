package com.flower_details.features.cart.application.usecase;

import com.flower_details.features.cart.application.exception.CartItemNotFoundException;
import com.flower_details.features.cart.application.exception.CartNotFoundException;
import com.flower_details.features.cart.domain.model.Cart;
import com.flower_details.features.cart.domain.model.CartItem;
import com.flower_details.features.cart.domain.repository.CartItemRepository;
import com.flower_details.features.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCartItemUseCase {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;

	@Transactional
	public void execute(Long customerId, Long itemId) {
		Cart cart = cartRepository.findActiveByCustomerIdForUpdate(customerId)
				.orElseThrow(CartNotFoundException::new);
		CartItem item = cartItemRepository.findActiveByIdAndCartId(itemId, cart.id())
				.orElseThrow(() -> new CartItemNotFoundException(itemId));
		cartItemRepository.delete(item);
	}
}
