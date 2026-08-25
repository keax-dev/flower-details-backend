package com.flower_details.features.category.application.usecase;

import com.flower_details.features.category.application.dto.view.CategoryView;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RetrieveCategoriesUseCase {

	private final CategoryRepository categoryRepository;

	@Transactional(readOnly = true)
	public PageResult<CategoryView> executePublicCatalog(PageRequest pageRequest) {
		return categoryRepository.findAllActive(pageRequest).map(CategoryView::from);
	}

	@Transactional(readOnly = true)
	public PageResult<CategoryView> executeForAdministration(PageRequest pageRequest) {
		return categoryRepository.findAll(pageRequest).map(CategoryView::from);
	}
}
