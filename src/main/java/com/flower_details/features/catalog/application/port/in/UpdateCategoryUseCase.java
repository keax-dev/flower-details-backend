package com.flower_details.features.catalog.application.port.in;

import com.flower_details.features.catalog.application.dto.CategoryView;
import com.flower_details.features.catalog.application.dto.UpdateCategoryCommand;

public interface UpdateCategoryUseCase {

	CategoryView updateCategory(UpdateCategoryCommand command);
}
