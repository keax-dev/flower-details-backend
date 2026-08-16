package com.flower_details.features.cart.application.usecase;

import com.flower_details.features.cart.application.dto.view.CartView;
import com.flower_details.features.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCartUseCase {

	private final CartRepository cartRepository;
	private final CartViewAssembler cartViewAssembler;

	@Transactional(readOnly = true)
	public CartView execute(Long customerId) {
		return cartRepository.findActiveByCustomerId(customerId)
				.map(cartViewAssembler::from)
				.orElseGet(CartView::empty);
	}
}
