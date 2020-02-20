package eu.prismacapacity.cryptoshred.metrics;

public interface CryptoMetrics {

	default void notifyMissingKey() {
	}

	default void notifyDecryptionSuccess() {
	}

	default void notifyDecryptionFailure(Exception e) {
	}

	default void notifyKeyLookUp() {
	}

	default void notifyKeyCreation() {
	}

	default void timed(String timerName, Runnable fn) {
		fn.run();
	}

	default <T> T timed(String timerName, MetricsCallable<T> fn) {
		return fn.call();
	}

	static class NOP implements CryptoMetrics {
	}

}
