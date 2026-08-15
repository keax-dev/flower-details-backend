package com.flower_details.features.order.presentation.dto.request;
import com.flower_details.features.order.application.dto.command.CreateOrderCommand;
import com.flower_details.features.order.domain.model.FulfillmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
		@NotNull FulfillmentType fulfillmentType,
		@NotBlank @Size(max = 160) String contactName,
		@NotBlank @Size(max = 30) String contactPhone,
		@Size(max = 500) String deliveryAddress,
		@Size(max = 1_000) String additionalInstructions
) {

	public CreateOrderCommand toCommand() {
		return new CreateOrderCommand(
				fulfillmentType,
				contactName,
				contactPhone,
				deliveryAddress,
				additionalInstructions
		);
	}
}
