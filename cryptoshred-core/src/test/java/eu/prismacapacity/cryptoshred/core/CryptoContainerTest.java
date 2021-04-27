/*
 * Copyright © 2021 PRISMA European Capacity Platform GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  @Mock CryptoEngine engine;
  @Mock CryptoKeyRepository keyRepo;
  @Mock CryptoMetrics metrics;
  @Mock ObjectMapper mapper;

  CryptoAlgorithm algo = CryptoAlgorithm.AES_CBC;
  CryptoKeySize size = CryptoKeySize.BIT_256;
  CryptoSubjectId subjectId = CryptoSubjectId.of(UUID.randomUUID());

  CryptoContainer<Integer> withoutMetrics() {
    return CryptoContainer.fromDeserialization(
        Integer.class, algo, size, subjectId, new byte[32], engine, keyRepo, null, mapper);
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
        when(keyRepo.findKeyFor(any(), any(), any()))
            .thenReturn(Optional.of(mock(CryptoKey.class)));
        when(engine.decrypt(any(), any(), any())).thenThrow(IllegalStateException.class);
      }
    }

    @Nested
    class WithDecryptionSuccess extends DecryptionTest {
      @BeforeEach
      void setup() {
        when(keyRepo.findKeyFor(any(), any(), any()))
            .thenReturn(Optional.of(mock(CryptoKey.class)));
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
