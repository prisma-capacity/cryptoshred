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
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import lombok.NonNull;

/**
 * Encapsulates the actual encryption / decryption using jdk or bouncy castle
 *
 * @author uwe
 */
public interface CryptoEngine {

  @NonNull
  byte[] decrypt(
      @NonNull CryptoAlgorithm algo, @NonNull CryptoKey cryptoKey, @NonNull byte[] bytes);

  @NonNull
  byte[] encrypt(
      @NonNull byte[] unencypted, @NonNull CryptoAlgorithm algorithm, @NonNull CryptoKey key);

  @NonNull
  CryptoKey generateKey(@NonNull CryptoAlgorithm algo, @NonNull CryptoKeySize size);
}
