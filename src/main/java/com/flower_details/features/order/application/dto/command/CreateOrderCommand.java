package com.flower_details.features.order.application.dto.command;

import com.flower_details.features.order.domain.model.FulfillmentType;

public record CreateOrderCommand(
		FulfillmentType fulfillmentType,
		String contactName,
		String contactPhone,
		String deliveryAddress,
		String additionalInstructions
) {
}
