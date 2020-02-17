package eu.prismacapacity.cryptoshred.metrics;

import java.io.IOException;

public interface CryptoMetrics {

	default void notifyMissingKey() {
	}

	default void notifyDecryptionSuccess() {
	}

	default void notifyDecryptionFailure(IOException e) {
	}

	static class NOP implements CryptoMetrics {
	}

}
