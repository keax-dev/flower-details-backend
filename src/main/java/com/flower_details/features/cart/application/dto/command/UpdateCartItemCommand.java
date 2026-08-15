package com.flower_details.features.cart.application.dto.command;

public record UpdateCartItemCommand(Long itemId, int quantity) {
}
