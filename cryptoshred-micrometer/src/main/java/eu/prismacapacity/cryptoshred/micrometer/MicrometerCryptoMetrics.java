package eu.prismacapacity.cryptoshred.micrometer;

import eu.prismacapacity.cryptoshred.core.metrics.CryptoMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

public class MicrometerCryptoMetrics implements CryptoMetrics {

	final MeterRegistry reg = Metrics.globalRegistry;
	private Counter missingKey;
	private Counter decryptionSuccess;
	private Counter decryptionFailure;

	public MicrometerCryptoMetrics() {
		missingKey = reg.counter("cryptoshred_missing_key");
		decryptionSuccess = reg.counter("cryptoshred_decryption_success");
		decryptionFailure = reg.counter("cryptoshred_decryption_failure");
	}

	@Override
	public void notifyMissingKey() {
		missingKey.increment();
	}

	@Override
	public void notifyDecryptionSuccess() {
		decryptionSuccess.count();
	}

	@Override
	public void notifyDecryptionFailure(Exception e) {
		decryptionFailure.count();
	}

}
