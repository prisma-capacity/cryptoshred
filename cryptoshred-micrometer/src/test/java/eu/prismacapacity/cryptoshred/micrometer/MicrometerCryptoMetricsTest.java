package eu.prismacapacity.cryptoshred.micrometer;

import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.micrometer.core.instrument.Counter;

@ExtendWith(MockitoExtension.class)
public class MicrometerCryptoMetricsTest {
	@Mock
	private Counter missingKey;

	@Mock
	private Counter decryptionSuccess;
	@Mock
	private Counter decryptionFailure;

	private MicrometerCryptoMetrics uut;

	@Test
	void notifyDecryptionFailure() throws Exception {
		uut = new MicrometerCryptoMetrics(missingKey, decryptionSuccess, decryptionFailure);

		uut.notifyDecryptionFailure(new IOException());
		verify(decryptionFailure).increment();
		verifyNoMoreInteractions(decryptionFailure);
		verifyZeroInteractions(missingKey, decryptionSuccess);
	}

	@Test
	void notifyMissingKey() throws Exception {
		uut = new MicrometerCryptoMetrics(missingKey, decryptionSuccess, decryptionFailure);

		uut.notifyMissingKey();
		verify(missingKey).increment();
		verifyNoMoreInteractions(missingKey);
		verifyZeroInteractions(decryptionFailure, decryptionSuccess);
	}

	@Test
	void notifyDecryptionSuccess() throws Exception {
		uut = new MicrometerCryptoMetrics(missingKey, decryptionSuccess, decryptionFailure);

		uut.notifyDecryptionSuccess();
		verify(decryptionSuccess).increment();
		verifyNoMoreInteractions(decryptionSuccess);
		verifyZeroInteractions(decryptionFailure, missingKey);
	}
}
