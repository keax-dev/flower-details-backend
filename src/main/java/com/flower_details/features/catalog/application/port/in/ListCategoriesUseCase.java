package com.flower_details.features.catalog.application.port.in;

import com.flower_details.features.catalog.application.dto.CategoryView;

import java.util.List;

public interface ListCategoriesUseCase {

	List<CategoryView> listActiveCategories();
}
