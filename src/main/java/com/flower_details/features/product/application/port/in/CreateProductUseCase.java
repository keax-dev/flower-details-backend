package com.flower_details.features.product.application.port.in;

import com.flower_details.features.product.application.dto.CreateProductCommand;
import com.flower_details.features.product.application.dto.ProductView;

public interface CreateProductUseCase {

	ProductView createProduct(CreateProductCommand command);
}
