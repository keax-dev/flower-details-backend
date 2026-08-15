package com.flower_details.features.cart.infrastructure.persistence.repository;

import com.flower_details.features.cart.domain.model.Cart;
import com.flower_details.features.cart.domain.model.CartStatus;
import com.flower_details.features.cart.domain.repository.CartRepository;
import com.flower_details.features.cart.infrastructure.persistence.mapper.CartPersistenceMapper;
import com.flower_details.features.users.infrastructure.persistence.entity.UserJpaEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaCartRepository implements CartRepository {

	private final SpringDataCartJpaRepository repository;
	private final EntityManager entityManager;

	@Override
	public Cart save(Cart cart) {
		UserJpaEntity customer = entityManager.getReference(UserJpaEntity.class, cart.customerId());
		return CartPersistenceMapper.toDomain(repository.save(CartPersistenceMapper.toEntity(cart, customer)));
	}

	@Override
	public Optional<Cart> findActiveByCustomerId(Long customerId) {
		return repository.findByCustomer_IdAndStatus(customerId, CartStatus.ACTIVE).map(CartPersistenceMapper::toDomain);
	}

	@Override
	public Optional<Cart> findActiveByCustomerIdForUpdate(Long customerId) {
		return repository.findWithLockByCustomer_IdAndStatus(customerId, CartStatus.ACTIVE)
				.map(CartPersistenceMapper::toDomain);
	}
}
