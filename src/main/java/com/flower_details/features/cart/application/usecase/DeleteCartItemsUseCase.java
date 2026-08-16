package com.flower_details.features.cart.application.usecase;

import com.flower_details.features.cart.domain.repository.CartItemRepository;
import com.flower_details.features.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteCartItemsUseCase {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;

	@Transactional
	public void execute(Long customerId) {
		cartRepository.findActiveByCustomerIdForUpdate(customerId)
				.ifPresent(cart -> cartItemRepository.deleteAllActiveByCartId(cart.id()));
	}
}
