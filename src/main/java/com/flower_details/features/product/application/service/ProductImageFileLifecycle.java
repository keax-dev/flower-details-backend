package com.flower_details.features.product.application.service;

import com.flower_details.features.product.application.port.out.ProductImageStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Slf4j
@RequiredArgsConstructor
class ProductImageFileLifecycle {

	private final ProductImageStoragePort productImageStorage;

	void deleteAfterCommit(String storedFileName) {
		register(storedFileName, true);
	}

	void deleteAfterRollback(String storedFileName) {
		register(storedFileName, false);
	}

	private void register(String storedFileName, boolean afterCommit) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			deleteQuietly(storedFileName);
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				boolean committed = status == STATUS_COMMITTED;
				if (committed == afterCommit) {
					deleteQuietly(storedFileName);
				}
			}
		});
	}

	private void deleteQuietly(String storedFileName) {
		try {
			productImageStorage.delete(storedFileName);
		}
		catch (RuntimeException exception) {
			log.error("No se pudo eliminar el archivo de imagen {}", storedFileName, exception);
		}
	}
}
