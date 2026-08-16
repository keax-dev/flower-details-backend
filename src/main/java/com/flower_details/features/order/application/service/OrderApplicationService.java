package com.flower_details.features.order.application.service;

import com.flower_details.features.cart.domain.model.Cart;
import com.flower_details.features.cart.domain.model.CartItem;
import com.flower_details.features.cart.domain.repository.CartItemRepository;
import com.flower_details.features.cart.domain.repository.CartRepository;
import com.flower_details.features.order.application.dto.command.CreateOrderCommand;
import com.flower_details.features.order.application.dto.view.OrderAuditView;
import com.flower_details.features.order.application.dto.view.OrderView;
import com.flower_details.features.order.application.exception.OrderNotFoundException;
import com.flower_details.features.order.domain.model.OrderAudit;
import com.flower_details.features.order.domain.model.OrderAuditAction;
import com.flower_details.features.order.domain.model.Order;
import com.flower_details.features.order.domain.model.OrderItem;
import com.flower_details.features.order.domain.model.OrderSearchCriteria;
import com.flower_details.features.order.domain.model.OrderStatus;
import com.flower_details.features.order.domain.repository.OrderItemRepository;
import com.flower_details.features.order.domain.repository.OrderAuditRepository;
import com.flower_details.features.order.domain.repository.OrderRepository;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.model.ProductImage;
import com.flower_details.features.product.domain.repository.ProductImageRepository;
import com.flower_details.features.product.domain.repository.ProductRepository;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.shared.domain.DomainException;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final ProductRepository productRepository;
	private final ProductImageRepository imageRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final OrderAuditRepository orderAuditRepository;
	private final UserRepository userRepository;

	@Transactional
	public OrderView create(Long customerId, CreateOrderCommand command) {
		Cart cart = findActiveCartForCheckout(customerId);
		List<CartItem> cartItems = findCartItems(cart.id());
		Map<Long, Product> availableProducts = findAvailableProducts(cartItems);

		Order order = orderRepository.save(Order.create(
				nextOrderNumber(),
				customerId,
				command.fulfillmentType(),
				calculateTotal(cartItems),
				command.contactName(),
				command.contactPhone(),
				command.deliveryAddress(),
				command.additionalInstructions()
		));
		recordAudit(order, customerId, OrderAuditAction.CREATED, null, null);

		List<OrderItem> items = orderItemRepository.saveAll(toOrderItems(order.id(), cartItems, availableProducts));
		cartItemRepository.deleteAllActiveByCartId(cart.id());
		return OrderView.from(order, items);
	}

	@Transactional(readOnly = true)
	public PageResult<OrderView> myOrders(Long customerId, OrderSearchCriteria criteria, PageRequest pageRequest) {
		return toViews(orderRepository.search(criteria.forCustomer(customerId), pageRequest));
	}

	@Transactional(readOnly = true)
	public PageResult<OrderView> allOrders(OrderSearchCriteria criteria, PageRequest pageRequest) {
		return toViews(orderRepository.search(criteria, pageRequest));
	}

	@Transactional(readOnly = true)
	public OrderView get(Long id, Long requesterId, UserRole role) {
		Order order = findOrder(id);
		ensureCanViewOrder(order, requesterId, role);
		return toView(order);
	}

	@Transactional(readOnly = true)
	public List<OrderAuditView> auditTrail(Long id, Long requesterId, UserRole role) {
		Order order = findOrder(id);
		ensureCanViewOrder(order, requesterId, role);
		return orderAuditRepository.findByOrderId(order.id()).stream().map(OrderAuditView::from).toList();
	}

	@Transactional
	public OrderView assign(Long id, Long requesterId, UserRole role, Long requestedOperatorId) {
		Order order = findOrder(id);
		Long operatorId = role == UserRole.OPERATOR ? requesterId : requireOperatorId(requestedOperatorId);
		ensureActiveOperator(operatorId);

		OrderStatus previousStatus = order.status();
		order.assignTo(operatorId, Instant.now());
		Order savedOrder = orderRepository.save(order);
		recordAudit(savedOrder, requesterId, OrderAuditAction.ASSIGNED, previousStatus, "Operador asignado: " + operatorId);
		return toView(savedOrder);
	}

	@Transactional
	public OrderView changeStatus(Long id, Long requesterId, UserRole role, OrderStatus status) {
		Order order = findOrder(id);
		ensureCanManageOrder(order, requesterId, role);

		OrderStatus previousStatus = order.status();
		order.changeStatus(status, Instant.now());
		Order savedOrder = orderRepository.save(order);
		recordAudit(savedOrder, requesterId, OrderAuditAction.STATUS_CHANGED, previousStatus, null);
		return toView(savedOrder);
	}

	@Transactional
	public void cancel(Long id, Long requesterId, UserRole role, String reason) {
		Order order = findOrder(id);
		if (role == UserRole.CUSTOMER
				&& (!order.customerId().equals(requesterId) || order.status() != OrderStatus.GENERATED)) {
			throw new DomainException("El cliente solo puede cancelar sus pedidos generados");
		}

		OrderStatus previousStatus = order.status();
		order.cancel(reason, Instant.now());
		Order savedOrder = orderRepository.save(order);
		recordAudit(savedOrder, requesterId, OrderAuditAction.CANCELLED, previousStatus, reason);
	}

	private Cart findActiveCartForCheckout(Long customerId) {
		return cartRepository.findActiveByCustomerIdForUpdate(customerId)
				.orElseThrow(() -> new DomainException("El carrito esta vacio"));
	}

	private List<CartItem> findCartItems(Long cartId) {
		List<CartItem> cartItems = cartItemRepository.findActiveByCartId(cartId);
		if (cartItems.isEmpty()) {
			throw new DomainException("El carrito esta vacio");
		}
		return cartItems;
	}

	private Map<Long, Product> findAvailableProducts(List<CartItem> cartItems) {
		Map<Long, Product> productsById = productRepository.findActiveByIds(
				cartItems.stream().map(CartItem::productId).toList()
		).stream().collect(Collectors.toMap(Product::id, Function.identity()));

		if (productsById.size() != cartItems.size()) {
			throw new DomainException("El carrito contiene productos no disponibles");
		}
		return productsById;
	}

	private List<OrderItem> toOrderItems(Long orderId, List<CartItem> cartItems, Map<Long, Product> productsById) {
		Map<Long, ProductImage> imagesByProductId = imageRepository.findActiveByProductIds(productsById.keySet()).stream()
				.collect(Collectors.toMap(ProductImage::productId, Function.identity(), (first, ignored) -> first));

		return cartItems.stream()
				.map(cartItem -> toOrderItem(orderId, cartItem, productsById.get(cartItem.productId()),
						imagesByProductId.get(cartItem.productId())))
				.toList();
	}

	private OrderItem toOrderItem(Long orderId, CartItem cartItem, Product product, ProductImage image) {
		return OrderItem.create(
				orderId,
				product.id(),
				product.title(),
				image == null ? null : image.url(),
				cartItem.quantity(),
				cartItem.unitPrice()
		);
	}

	private BigDecimal calculateTotal(List<CartItem> cartItems) {
		return cartItems.stream().map(CartItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private Long requireOperatorId(Long operatorId) {
		if (operatorId == null) {
			throw new DomainException("El operador es obligatorio");
		}
		return operatorId;
	}

	private void ensureActiveOperator(Long operatorId) {
		User operator = userRepository.findById(operatorId)
				.orElseThrow(() -> new DomainException("El operador no existe"));
		if (!operator.active() || operator.role() != UserRole.OPERATOR) {
			throw new DomainException("El usuario asignado debe ser un operador activo");
		}
	}

	private void ensureCanManageOrder(Order order, Long requesterId, UserRole role) {
		if (role == UserRole.OPERATOR && !requesterId.equals(order.assignedOperatorId())) {
			throw new DomainException("El operador solo puede gestionar pedidos que tiene asignados");
		}
	}

	private void ensureCanViewOrder(Order order, Long requesterId, UserRole role) {
		if (role == UserRole.CUSTOMER && !order.customerId().equals(requesterId)) {
			throw new OrderNotFoundException(order.id());
		}
	}

	private void recordAudit(
			Order order,
			Long actorUserId,
			OrderAuditAction action,
			OrderStatus previousStatus,
			String details
	) {
		orderAuditRepository.save(OrderAudit.create(
				order.id(), actorUserId, action, previousStatus, order.status(), details, Instant.now()
		));
	}

	private Order findOrder(Long id) {
		return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
	}

	private OrderView toView(Order order) {
		return OrderView.from(order, orderItemRepository.findByOrderIds(List.of(order.id())));
	}

	private PageResult<OrderView> toViews(PageResult<Order> page) {
		Map<Long, List<OrderItem>> itemsByOrderId = orderItemRepository.findByOrderIds(
				page.items().stream().map(Order::id).toList()
		).stream().collect(Collectors.groupingBy(OrderItem::orderId));

		return page.map(order -> OrderView.from(order, itemsByOrderId.getOrDefault(order.id(), List.of())));
	}

	private String nextOrderNumber() {
		String date = LocalDate.now(ZoneOffset.UTC).toString().replace("-", "");
		String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
		return "FD-" + date + "-" + suffix;
	}
}
