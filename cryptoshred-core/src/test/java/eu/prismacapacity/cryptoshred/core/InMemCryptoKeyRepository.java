/*
 * Copyright © 2020 PRISMA European Capacity Platform GmbH
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

import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyNotFoundAfterCreatingException;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyRepository;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemCryptoKeyRepository implements CryptoKeyRepository {

  final Map<CryptoSubjectId, CryptoKey> keys = new HashMap<>();
  private final CryptoEngine engine;

  public InMemCryptoKeyRepository(CryptoEngine engine) {
    this.engine = engine;
  }

  @Override
  public synchronized Optional<CryptoKey> findKeyFor(
      CryptoSubjectId subjectId, CryptoAlgorithm algo, CryptoKeySize size) {
    return Optional.ofNullable(keys.get(subjectId));
  }

  @Override
  public synchronized CryptoKey getOrCreateKeyFor(
      CryptoSubjectId subjectId, CryptoAlgorithm algo, CryptoKeySize size)
      throws CryptoKeyNotFoundAfterCreatingException {
    Optional<CryptoKey> existingKey = findKeyFor(subjectId, algo, size);
    return existingKey.orElseGet(
        () -> {
          CryptoKey key;

          key = engine.generateKey(algo, size);
          keys.put(subjectId, key);
          return key;
        });
  }
}
