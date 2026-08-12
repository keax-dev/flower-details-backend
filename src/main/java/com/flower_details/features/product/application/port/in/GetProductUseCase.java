package com.flower_details.features.product.application.port.in;

import com.flower_details.features.product.application.dto.ProductView;

public interface GetProductUseCase {

	ProductView getActiveProduct(Long id);
}
