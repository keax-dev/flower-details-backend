package com.flower_details.features.cart.application.assembler;

import com.flower_details.features.cart.application.dto.view.CartItemView;
import com.flower_details.features.cart.application.dto.view.CartProductView;
import com.flower_details.features.cart.application.dto.view.CartView;
import com.flower_details.features.cart.domain.model.Cart;
import com.flower_details.features.cart.domain.model.CartItem;
import com.flower_details.features.cart.domain.repository.CartItemRepository;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.model.ProductImage;
import com.flower_details.features.product.domain.repository.ProductImageRepository;
import com.flower_details.features.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartViewAssembler {

	private final CartItemRepository cartItemRepository;
	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;

	public CartView from(Cart cart) {
		List<CartItem> items = cartItemRepository.findActiveByCartId(cart.id());
		if (items.isEmpty()) {
			return CartView.from(cart, List.of());
		}
		Map<Long, Product> productsById = productRepository.findActiveByIds(items.stream().map(CartItem::productId).toList())
				.stream().collect(Collectors.toMap(Product::id, Function.identity()));
		Map<Long, ProductImage> primaryImages = productImageRepository.findActiveByProductIds(productsById.keySet())
				.stream().collect(Collectors.toMap(ProductImage::productId, Function.identity(), (first, ignored) -> first));
		return CartView.from(cart, items.stream().map(item -> {
			Product product = productsById.get(item.productId());
			CartProductView productView = product == null
					? CartProductView.unavailable(item.productId())
					: CartProductView.from(product, primaryImages.get(product.id()));
			return CartItemView.from(item, productView);
		}).toList());
	}
}
