package com.flower_details.features.order.presentation.dto.request;

import com.flower_details.features.order.domain.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeOrderStatusRequest(@NotNull OrderStatus status) {
}
