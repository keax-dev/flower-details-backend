package com.flower_details.features.cart.application.usecase;

import com.flower_details.features.cart.application.dto.command.UpdateCartItemCommand;
import com.flower_details.features.cart.application.dto.view.CartView;
import com.flower_details.features.cart.application.assembler.CartViewAssembler;
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
public class UpdateCartItemUseCase {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final CartViewAssembler cartViewAssembler;

	@Transactional
	public CartView execute(Long customerId, UpdateCartItemCommand command) {
		Cart cart = cartRepository.findActiveByCustomerIdForUpdate(customerId)
				.orElseThrow(CartNotFoundException::new);
		CartItem item = cartItemRepository.findActiveByIdAndCartId(command.itemId(), cart.id())
				.orElseThrow(() -> new CartItemNotFoundException(command.itemId()));
		item.updateQuantity(command.quantity());
		cartItemRepository.save(item);
		return cartViewAssembler.from(cart);
	}
}
