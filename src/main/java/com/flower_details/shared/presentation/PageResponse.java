package com.flower_details.shared.presentation;

import com.flower_details.shared.domain.pagination.PageResult;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
		List<T> items,
		int page,
		int size,
		long totalElements,
		int totalPages
) {

	public static <T, R> PageResponse<R> from(PageResult<T> result, Function<T, R> mapper) {
		return new PageResponse<>(
				result.items().stream().map(mapper).toList(),
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages()
		);
	}
}
