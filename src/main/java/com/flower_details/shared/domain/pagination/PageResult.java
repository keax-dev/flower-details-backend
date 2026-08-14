package com.flower_details.shared.domain.pagination;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(
		List<T> items,
		int page,
		int size,
		long totalElements,
		int totalPages
) {

	public PageResult {
		items = List.copyOf(items);
	}

	public <R> PageResult<R> map(Function<? super T, R> mapper) {
		List<R> mappedItems = items.stream().map(mapper).toList();
		return new PageResult<>(mappedItems, page, size, totalElements, totalPages);
	}
}
