package com.flower_details.features.cart.application.usecase;

import com.flower_details.features.cart.application.dto.command.AddCartItemCommand;
import com.flower_details.features.cart.application.dto.view.CartView;
import com.flower_details.features.cart.application.exception.CartProductUnavailableException;
import com.flower_details.features.cart.domain.model.Cart;
import com.flower_details.features.cart.domain.model.CartItem;
import com.flower_details.features.cart.domain.repository.CartItemRepository;
import com.flower_details.features.cart.domain.repository.CartRepository;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddCartItemUseCase {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final ProductRepository productRepository;
	private final CartViewAssembler cartViewAssembler;

	@Transactional
	public CartView execute(Long customerId, AddCartItemCommand command) {
		Product product = productRepository.findActiveById(command.productId())
				.orElseThrow(() -> new CartProductUnavailableException(command.productId()));
		Cart cart = cartRepository.findActiveByCustomerIdForUpdate(customerId)
				.orElseGet(() -> cartRepository.save(Cart.create(customerId)));
		cartItemRepository.findActiveByCartIdAndProductId(cart.id(), product.id())
				.ifPresentOrElse(item -> {
					item.addQuantity(command.quantity());
					cartItemRepository.save(item);
				}, () -> cartItemRepository.save(CartItem.create(
						cart.id(), product.id(), command.quantity(), product.price()
				)));
		return cartViewAssembler.from(cart);
	}
}
