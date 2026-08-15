package com.flower_details.features.cart.application.dto.command;

public record AddCartItemCommand(Long productId, int quantity) {
}
