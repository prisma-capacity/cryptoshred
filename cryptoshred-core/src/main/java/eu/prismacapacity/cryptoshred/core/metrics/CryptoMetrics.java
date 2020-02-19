package eu.prismacapacity.cryptoshred.core.metrics;

public interface CryptoMetrics {

	default void notifyMissingKey() {
	}

	default void notifyDecryptionSuccess() {
	}

	default void notifyDecryptionFailure(Exception e) {
	}

	static class NOP implements CryptoMetrics {
	}

}
