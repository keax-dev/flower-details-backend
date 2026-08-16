package com.flower_details.features.product.infrastructure.storage;

import com.flower_details.features.product.application.service.ProductImageFileLifecycle;
import com.flower_details.features.product.application.service.ProductImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@Slf4j
@RequiredArgsConstructor
class TransactionSynchronizedProductImageFileLifecycle implements ProductImageFileLifecycle {

	private final ProductImageStorage productImageStorage;

	@Override
	public void deleteAfterCommit(String storedFileName) {
		register(storedFileName, true);
	}

	@Override
	public void deleteAfterRollback(String storedFileName) {
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
