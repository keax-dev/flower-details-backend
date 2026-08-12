package com.flower_details.features.product.application.port.in;

import com.flower_details.features.product.application.dto.ProductView;

import java.util.List;

public interface ListProductsUseCase {

	List<ProductView> listActiveProducts();
}
