package eu.prismacapacity.cryptoshred.core;

import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyRepository;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import eu.prismacapacity.cryptoshred.core.metrics.CryptoMetrics;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CryptoContainerTest {
  @Mock
  CryptoEngine engine;
  @Mock
  CryptoKeyRepository keyRepo;
  @Mock
  CryptoMetrics metrics;
  @Mock
  ObjectMapper mapper;

  CryptoAlgorithm algo = CryptoAlgorithm.AES_CBC;
  CryptoKeySize size = CryptoKeySize.BIT_256;
  CryptoSubjectId subjectId = CryptoSubjectId.of(UUID.randomUUID());

  CryptoContainer<Integer> withoutMetrics() {
    return CryptoContainer.fromDeserialization(
            Integer.class, algo, size, subjectId
            , new byte[32],
            engine, keyRepo, null, mapper
    );
  }

  @Nested
  class WhenDecrypting {

    class DecryptionTest {
      @Test
      void ignoresNullMetrics() {
        // must not throw NPE
        withoutMetrics().value();
      }
    }

    @Nested
    class WithDecryptionFailure extends DecryptionTest {
      @BeforeEach
      void setup() {
        when(keyRepo.findKeyFor(any(), any(), any())).thenReturn(Optional.of(mock(CryptoKey.class)));
        when(engine.decrypt(any(), any(), any())).thenThrow(IllegalStateException.class);
      }
    }

    @Nested
    class WithDecryptionSuccess extends DecryptionTest {
      @BeforeEach
      void setup() {
        when(keyRepo.findKeyFor(any(), any(), any())).thenReturn(Optional.of(mock(CryptoKey.class)));
        when(engine.decrypt(any(), any(), any())).thenReturn(new byte[32]);
        when(mapper.readerFor(any(Class.class))).thenReturn(mock(ObjectReader.class));
      }
    }

    @Nested
    class WithMissingKey extends DecryptionTest {
      @BeforeEach
      void setup() {
        when(keyRepo.findKeyFor(any(), any(), any())).thenReturn(Optional.empty());
      }
    }
  }
}
