/*
 * Copyright © 2026 PRISMA European Capacity Platform GmbH 
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

import java.security.SecureRandom;
import java.util.*;
import javax.crypto.spec.IvParameterSpec;
import lombok.NonNull;

public abstract class AbstractCryptoEngine implements CryptoEngine {
  protected final CryptoInitializationVector configuredInitVector;

  protected final boolean useRandomInitVector;

  protected final Map<CryptoAlgorithm, String> exactCipherNames = createExactCipherMapping();

  protected static final SecureRandom RANDOM = new SecureRandom();

  protected AbstractCryptoEngine(String configuredInitVectorOrNull, boolean useRandomInitVector) {
    if (!useRandomInitVector && null == configuredInitVectorOrNull) {
      throw new IllegalArgumentException(
          "No init vector configured, and useRandomInitVector is false.");
    }

    this.useRandomInitVector = useRandomInitVector;

    if (configuredInitVectorOrNull == null) {
      this.configuredInitVector = null;
    } else this.configuredInitVector = CryptoInitializationVector.of(configuredInitVectorOrNull);
  }

  private static Map<CryptoAlgorithm, String> createExactCipherMapping() {
    // initialize with known algorithms
    HashMap<CryptoAlgorithm, String> map = new HashMap<>();
    map.put(CryptoAlgorithm.AES_CBC, "AES/CBC/PKCS5PADDING");
    return Collections.unmodifiableMap(map);
  }

  @NonNull
  protected final IvParameterSpec resolveInitVectorForDecryption(
      IvParameterSpec initializationVectorProvidedOrNull) {

    if (initializationVectorProvidedOrNull != null) {
      return initializationVectorProvidedOrNull;
    } else {
      // no IV stored with the container, so we use the configured one
      if (configuredInitVector == null)
        throw new IllegalStateException(
            "No init vector configured, and none stored with the container.");
      else return configuredInitVector.getIvParameterSpec();
    }
  }

  public final @NonNull IvParameterSpec getInitVectorForEncryption() {

    if (useRandomInitVector) {
      byte[] iv = new byte[16];
      RANDOM.nextBytes(iv);
      return new IvParameterSpec(iv);
    } else
      // guaranteed by constructor check
      return configuredInitVector.getIvParameterSpec();
  }
}
