package com.flower_details.features.cart.infrastructure.persistence.repository;

import com.flower_details.features.cart.domain.model.Cart;
import com.flower_details.features.cart.domain.model.CartStatus;
import com.flower_details.features.cart.domain.repository.CartRepository;
import com.flower_details.features.cart.infrastructure.persistence.mapper.CartPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JpaCartRepository implements CartRepository {

	private final SpringDataCartJpaRepository repository;

	@Override
	public Cart save(Cart cart) {
		return CartPersistenceMapper.toDomain(repository.save(CartPersistenceMapper.toEntity(cart)));
	}

	@Override
	public Optional<Cart> findActiveByCustomerId(Long customerId) {
		return repository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE).map(CartPersistenceMapper::toDomain);
	}
}
