package com.flower_details.features.catalog.application.port.in;

import com.flower_details.features.catalog.application.dto.CategoryView;
import com.flower_details.features.catalog.application.dto.CreateCategoryCommand;

public interface CreateCategoryUseCase {

	CategoryView createCategory(CreateCategoryCommand command);
}
