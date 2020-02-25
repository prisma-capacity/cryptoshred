package eu.prismacapacity.cryptoshred.micrometer;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.function.Supplier;

import io.micrometer.core.instrument.Timer;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@ExtendWith(MockitoExtension.class)
public class MicrometerCryptoMetricsTest {

	@Mock
	private MeterRegistry registry;

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
		uut = new MicrometerCryptoMetrics(registry, missingKey, decryptionSuccess, decryptionFailure, keyLookUp,
				keyCreation, keyCreationAfterConflict);

		uut.notifyDecryptionFailure(new IOException());
		verify(decryptionFailure).increment();
		verifyNoMoreInteractions(decryptionFailure);
		verifyNoInteractions(missingKey, decryptionSuccess);
	}

	@Test
	void notifyMissingKey() throws Exception {
		uut = new MicrometerCryptoMetrics(registry, missingKey, decryptionSuccess, decryptionFailure, keyLookUp,
				keyCreation, keyCreationAfterConflict);

		uut.notifyMissingKey();
		verify(missingKey).increment();
		verifyNoMoreInteractions(missingKey);
		verifyNoInteractions(decryptionFailure, decryptionSuccess);
	}

	@Test
	void notifyDecryptionSuccess() throws Exception {
		uut = new MicrometerCryptoMetrics(registry, missingKey, decryptionSuccess, decryptionFailure, keyLookUp,
				keyCreation, keyCreationAfterConflict);

		uut.notifyDecryptionSuccess();
		verify(decryptionSuccess).increment();
		verifyNoMoreInteractions(decryptionSuccess);
		verifyNoInteractions(decryptionFailure, missingKey);
	}

	@Test
	void notifyKeyLookUp() throws Exception {
		uut = new MicrometerCryptoMetrics(registry, missingKey, decryptionSuccess, decryptionFailure, keyLookUp,
				keyCreation, keyCreationAfterConflict);

		uut.notifyKeyLookUp();
		verify(keyLookUp).increment();
		verifyNoMoreInteractions(keyLookUp);
	}

	@Test
	void notifyKeyCreation() throws Exception {
		uut = new MicrometerCryptoMetrics(registry, missingKey, decryptionSuccess, decryptionFailure, keyLookUp,
				keyCreation, keyCreationAfterConflict);

		uut.notifyKeyCreation();
		verify(keyCreation).increment();
		verifyNoMoreInteractions(keyCreation);
	}

	@Test
	void keyCreationAfterConflict() throws Exception {
		uut = new MicrometerCryptoMetrics(registry, missingKey, decryptionSuccess, decryptionFailure, keyLookUp,
				keyCreation, keyCreationAfterConflict);

		uut.notifyKeyCreationAfterConflict();
		verify(keyCreationAfterConflict).increment();
		verifyNoMoreInteractions(keyCreationAfterConflict);
	}

	@Test
	void timedCreateKey() throws Exception {
		uut = new MicrometerCryptoMetrics(registry, missingKey, decryptionSuccess, decryptionFailure, keyLookUp,
				keyCreation, keyCreationAfterConflict);

		val timer = mock(Timer.class);
		when(registry.timer("cryptoshred_create_key")).thenReturn(timer);
		when(timer.record(any(Supplier.class))).thenReturn(null);

		uut.timedCreateKey(() -> null);

		verify(timer).record(any(Supplier.class));
		verify(registry).timer("cryptoshred_create_key");
		verifyNoMoreInteractions(registry, timer);
	}

	@Test
	void timedFindKey() throws Exception {
		uut = new MicrometerCryptoMetrics(registry, missingKey, decryptionSuccess, decryptionFailure, keyLookUp,
				keyCreation, keyCreationAfterConflict);

		val timer = mock(Timer.class);
		when(registry.timer("cryptoshred_find_key")).thenReturn(timer);
		when(timer.record(any(Supplier.class))).thenReturn(null);

		uut.timedFindKey(() -> null);

		verify(timer).record(any(Supplier.class));
		verify(registry).timer("cryptoshred_find_key");
		verifyNoMoreInteractions(registry, timer);
	}
}
