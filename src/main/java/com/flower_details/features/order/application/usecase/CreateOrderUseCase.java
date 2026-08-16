package com.flower_details.features.order.application.usecase;

import com.flower_details.features.order.application.dto.command.CreateOrderCommand;
import com.flower_details.features.order.application.dto.view.OrderView;
import com.flower_details.features.cart.domain.model.Cart;
import com.flower_details.features.cart.domain.model.CartItem;
import com.flower_details.features.cart.domain.repository.CartItemRepository;
import com.flower_details.features.cart.domain.repository.CartRepository;
import com.flower_details.features.order.application.service.OrderNumberGenerator;
import com.flower_details.features.order.domain.model.Order;
import com.flower_details.features.order.domain.model.OrderAudit;
import com.flower_details.features.order.domain.model.OrderAuditAction;
import com.flower_details.features.order.domain.model.OrderItem;
import com.flower_details.features.order.domain.repository.OrderAuditRepository;
import com.flower_details.features.order.domain.repository.OrderItemRepository;
import com.flower_details.features.order.domain.repository.OrderRepository;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.model.ProductImage;
import com.flower_details.features.product.domain.repository.ProductImageRepository;
import com.flower_details.features.product.domain.repository.ProductRepository;
import com.flower_details.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final OrderAuditRepository orderAuditRepository;
	private final OrderNumberGenerator orderNumberGenerator;
	private final Clock clock;

	@Transactional
	public OrderView execute(Long customerId, CreateOrderCommand command) {
		Cart cart = cartRepository.findActiveByCustomerIdForUpdate(customerId)
				.orElseThrow(() -> new DomainException("El carrito esta vacio"));
		List<CartItem> cartItems = cartItemRepository.findActiveByCartId(cart.id());
		if (cartItems.isEmpty()) {
			throw new DomainException("El carrito esta vacio");
		}
		Map<Long, Product> productsById = productRepository.findActiveByIds(
				cartItems.stream().map(CartItem::productId).toList()
		).stream().collect(Collectors.toMap(Product::id, Function.identity()));
		if (productsById.size() != cartItems.size()) {
			throw new DomainException("El carrito contiene productos no disponibles");
		}
		Order order = orderRepository.save(Order.create(
				orderNumberGenerator.next(), customerId, command.fulfillmentType(),
				cartItems.stream().map(CartItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add),
				command.contactName(), command.contactPhone(), command.deliveryAddress(), command.additionalInstructions()
		));
		orderAuditRepository.save(OrderAudit.create(
				order.id(), customerId, OrderAuditAction.CREATED, null, order.status(), null, clock.instant()
		));
		Map<Long, ProductImage> imagesByProductId = productImageRepository
				.findActiveByProductIds(productsById.keySet()).stream()
				.collect(Collectors.toMap(ProductImage::productId, Function.identity(), (first, ignored) -> first));
		List<OrderItem> items = orderItemRepository.saveAll(cartItems.stream().map(cartItem -> {
			Product product = productsById.get(cartItem.productId());
			ProductImage image = imagesByProductId.get(cartItem.productId());
			return OrderItem.create(order.id(), product.id(), product.title(), image == null ? null : image.url(),
					cartItem.quantity(), cartItem.unitPrice());
		}).toList());
		cartItemRepository.deleteAllActiveByCartId(cart.id());
		return OrderView.from(order, items);
	}
}
