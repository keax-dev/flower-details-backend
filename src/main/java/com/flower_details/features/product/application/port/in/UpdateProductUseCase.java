package com.flower_details.features.product.application.port.in;

import com.flower_details.features.product.application.dto.ProductView;
import com.flower_details.features.product.application.dto.UpdateProductCommand;

public interface UpdateProductUseCase {

	ProductView updateProduct(UpdateProductCommand command);
}
