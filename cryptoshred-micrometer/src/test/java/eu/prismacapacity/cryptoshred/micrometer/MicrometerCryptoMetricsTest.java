package eu.prismacapacity.cryptoshred.micrometer;

import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MicrometerCryptoMetricsTest {
	@Mock
	private Counter missingKey;

	@Mock
	private Counter decryptionSuccess;

	@Mock
	private Counter decryptionFailure;

	@Mock
	private Counter keyLookUp;

	@Mock
	private Counter keyCreation;

	@Mock
	private Counter keyCreationAfterConflict;

	private MicrometerCryptoMetrics uut;

	@Test
	void notifyDecryptionFailure() throws Exception {
		uut = new MicrometerCryptoMetrics(missingKey, decryptionSuccess, decryptionFailure, keyLookUp, keyCreation, keyCreationAfterConflict);

		uut.notifyDecryptionFailure(new IOException());
		verify(decryptionFailure).increment();
		verifyNoMoreInteractions(decryptionFailure);
		verifyZeroInteractions(missingKey, decryptionSuccess);
	}

	@Test
	void notifyMissingKey() throws Exception {
		uut = new MicrometerCryptoMetrics(missingKey, decryptionSuccess, decryptionFailure, keyLookUp, keyCreation, keyCreationAfterConflict);

		uut.notifyMissingKey();
		verify(missingKey).increment();
		verifyNoMoreInteractions(missingKey);
		verifyZeroInteractions(decryptionFailure, decryptionSuccess);
	}

	@Test
	void notifyDecryptionSuccess() throws Exception {
		uut = new MicrometerCryptoMetrics(missingKey, decryptionSuccess, decryptionFailure, keyLookUp, keyCreation, keyCreationAfterConflict);

		uut.notifyDecryptionSuccess();
		verify(decryptionSuccess).increment();
		verifyNoMoreInteractions(decryptionSuccess);
		verifyZeroInteractions(decryptionFailure, missingKey);
	}

	@Test
	void notifyKeyLookUp() throws Exception {
		uut = new MicrometerCryptoMetrics(missingKey, decryptionSuccess, decryptionFailure, keyLookUp, keyCreation, keyCreationAfterConflict);

		uut.notifyKeyLookUp();
		verify(keyLookUp).increment();
		verifyNoMoreInteractions(keyLookUp);
		verifyZeroInteractions(keyLookUp, missingKey);
	}

	@Test
	void notifyKeyCreation() throws Exception {
		uut = new MicrometerCryptoMetrics(missingKey, decryptionSuccess, decryptionFailure, keyLookUp, keyCreation, keyCreationAfterConflict);

		uut.notifyKeyCreation();
		verify(keyCreation).increment();
		verifyNoMoreInteractions(keyCreation);
		verifyZeroInteractions(keyCreation, missingKey);
	}

	@Test
	void keyCreationAfterConflict() throws Exception {
		uut = new MicrometerCryptoMetrics(missingKey, decryptionSuccess, decryptionFailure, keyLookUp, keyCreation, keyCreationAfterConflict);

		uut.notifyKeyCreationAfterConflict();
		verify(keyCreationAfterConflict).increment();
		verifyNoMoreInteractions(keyCreationAfterConflict);
		verifyZeroInteractions(keyCreationAfterConflict, missingKey);
	}
}
