package eu.prismacapacity.cryptoshred.core.metrics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class CryptoMetricsBaseTest {

	final CryptoMetrics.Base uut = new CryptoMetrics.Base() {
	};

	@Test
	public void notifyMissingKey() {
		assertNotThrows(uut::notifyMissingKey);
	}

	@Test
	public void notifyDecryptionSuccess() {
		assertNotThrows(uut::notifyDecryptionSuccess);
	}

	@Test
	public void notifyDecryptionFailure() {
		assertNotThrows(() -> uut.notifyDecryptionFailure(new Exception()));
		assertThrows(NullPointerException.class, () -> uut.notifyDecryptionFailure(null));
	}

	public void assertNotThrows(Runnable r) {
		try {
			r.run();
		} catch (Exception e) {
			fail(e);
		}
	}
}
