package com.flower_details.features.product.application.service;

public interface ProductImageFileLifecycle {

	void deleteAfterCommit(String storedFileName);

	void deleteAfterRollback(String storedFileName);
}
