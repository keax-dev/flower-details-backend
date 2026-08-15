package com.flower_details.features.cart.infrastructure.persistence.repository;

import com.flower_details.features.cart.domain.model.CartItem;
import com.flower_details.features.cart.domain.repository.CartItemRepository;
import com.flower_details.features.cart.infrastructure.persistence.entity.CartJpaEntity;
import com.flower_details.features.cart.infrastructure.persistence.mapper.CartItemPersistenceMapper;
import com.flower_details.features.product.infrastructure.persistence.entity.ProductJpaEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaCartItemRepository implements CartItemRepository {

	private final SpringDataCartItemJpaRepository repository;
	private final EntityManager entityManager;

	@Override
	public CartItem save(CartItem item) {
		CartJpaEntity cart = entityManager.getReference(CartJpaEntity.class, item.cartId());
		ProductJpaEntity product = entityManager.getReference(ProductJpaEntity.class, item.productId());
		return CartItemPersistenceMapper.toDomain(repository.save(CartItemPersistenceMapper.toEntity(item, cart, product)));
	}

	@Override
	public List<CartItem> findActiveByCartId(Long cartId) {
		return repository.findAllByCart_IdOrderByCreatedAtAsc(cartId).stream().map(CartItemPersistenceMapper::toDomain).toList();
	}

	@Override
	public Optional<CartItem> findActiveByCartIdAndProductId(Long cartId, Long productId) {
		return repository.findByCart_IdAndProduct_Id(cartId, productId).map(CartItemPersistenceMapper::toDomain);
	}

	@Override
	public Optional<CartItem> findActiveByIdAndCartId(Long itemId, Long cartId) {
		return repository.findByIdAndCart_Id(itemId, cartId).map(CartItemPersistenceMapper::toDomain);
	}

	@Override
	public void delete(CartItem item) {
		repository.findByIdAndCart_Id(item.id(), item.cartId()).ifPresent(repository::delete);
	}

	@Override
	public void deleteAllActiveByCartId(Long cartId) {
		repository.findAllByCart_IdOrderByCreatedAtAsc(cartId).forEach(repository::delete);
	}
}
