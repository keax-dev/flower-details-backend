package com.flower_details.features.cart.application.service;

import com.flower_details.features.cart.application.dto.command.AddCartItemCommand;
import com.flower_details.features.cart.application.dto.command.UpdateCartItemCommand;
import com.flower_details.features.cart.application.dto.view.CartItemView;
import com.flower_details.features.cart.application.dto.view.CartProductView;
import com.flower_details.features.cart.application.dto.view.CartView;
import com.flower_details.features.cart.application.exception.CartItemNotFoundException;
import com.flower_details.features.cart.application.exception.CartNotFoundException;
import com.flower_details.features.cart.application.exception.CartProductUnavailableException;
import com.flower_details.features.cart.domain.model.Cart;
import com.flower_details.features.cart.domain.model.CartItem;
import com.flower_details.features.cart.domain.repository.CartItemRepository;
import com.flower_details.features.cart.domain.repository.CartRepository;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.model.ProductImage;
import com.flower_details.features.product.domain.repository.ProductImageRepository;
import com.flower_details.features.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartApplicationService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;

	@Transactional(readOnly = true)
	public CartView getCart(Long customerId) {
		return cartRepository.findActiveByCustomerId(customerId)
				.map(this::buildCartView)
				.orElseGet(CartView::empty);
	}

	@Transactional
	public CartView addItem(Long customerId, AddCartItemCommand command) {
		Product product = productRepository.findActiveById(command.productId())
				.orElseThrow(() -> new CartProductUnavailableException(command.productId()));
		Cart cart = cartRepository.findActiveByCustomerId(customerId)
				.orElseGet(() -> cartRepository.save(Cart.create(customerId)));

		cartItemRepository.findActiveByCartIdAndProductId(cart.id(), product.id())
				.ifPresentOrElse(
						item -> {
							item.addQuantity(command.quantity());
							cartItemRepository.save(item);
						},
						() -> cartItemRepository.save(CartItem.create(cart.id(), product.id(), command.quantity(), product.price()))
				);

		return buildCartView(cart);
	}

	@Transactional
	public CartView updateItem(Long customerId, UpdateCartItemCommand command) {
		Cart cart = findActiveCart(customerId);
		CartItem item = cartItemRepository.findActiveByIdAndCartId(command.itemId(), cart.id())
				.orElseThrow(() -> new CartItemNotFoundException(command.itemId()));
		item.updateQuantity(command.quantity());
		cartItemRepository.save(item);
		return buildCartView(cart);
	}

	@Transactional
	public void deleteItem(Long customerId, Long itemId) {
		Cart cart = findActiveCart(customerId);
		CartItem item = cartItemRepository.findActiveByIdAndCartId(itemId, cart.id())
				.orElseThrow(() -> new CartItemNotFoundException(itemId));
		cartItemRepository.delete(item);
	}

	@Transactional
	public void clearCart(Long customerId) {
		cartRepository.findActiveByCustomerId(customerId)
				.ifPresent(cart -> cartItemRepository.deleteAllActiveByCartId(cart.id()));
	}

	private Cart findActiveCart(Long customerId) {
		return cartRepository.findActiveByCustomerId(customerId)
				.orElseThrow(CartNotFoundException::new);
	}

	private CartView buildCartView(Cart cart) {
		List<CartItem> items = cartItemRepository.findActiveByCartId(cart.id());
		if (items.isEmpty()) {
			return CartView.from(cart, List.of());
		}

		Map<Long, Product> productsById = productRepository.findByIds(items.stream().map(CartItem::productId).toList())
				.stream().collect(Collectors.toMap(Product::id, Function.identity()));
		Map<Long, ProductImage> primaryImagesByProductId = productImageRepository
				.findActiveByProductIds(productsById.keySet()).stream()
				.collect(Collectors.toMap(ProductImage::productId, Function.identity(), (first, ignored) -> first));

		List<CartItemView> itemViews = items.stream().map(item -> {
			Product product = productsById.get(item.productId());
			if (product == null) {
				throw new CartProductUnavailableException(item.productId());
			}
			return CartItemView.from(item, CartProductView.from(product, primaryImagesByProductId.get(product.id())));
		}).toList();
		return CartView.from(cart, itemViews);
	}
}
