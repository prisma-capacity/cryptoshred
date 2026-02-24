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

import eu.prismacapacity.cryptoshred.core.keys.*;
import lombok.NonNull;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.*;

public class JDKCryptoEngine implements CryptoEngine {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final Map<CryptoAlgorithm, String> exactCipherNames = createExactCipherMapping();

  public JDKCryptoEngine(String configuredInitVectorOrNull, boolean useRandomInitVector) {
    if (configuredInitVectorOrNull == null) {
      this.configuredInitVector = null;
    } else this.configuredInitVector = CryptoInitializationVector.of(configuredInitVectorOrNull);

    this.useRandomInitVector = useRandomInitVector;
  }

  private static Map<CryptoAlgorithm, String> createExactCipherMapping() {
    // initialize with known algorithms
    HashMap<CryptoAlgorithm, String> map = new HashMap<>();
    map.put(CryptoAlgorithm.AES_CBC, "AES/CBC/PKCS5PADDING");
    return Collections.unmodifiableMap(map);
  }

  private final CryptoInitializationVector configuredInitVector;

  private final boolean useRandomInitVector;

  @Override
  public byte[] decrypt(
      @NonNull CryptoAlgorithm algo,
      @NonNull CryptoKey cryptoKey,
      @NonNull byte[] bytes,
      IvParameterSpec initializationVectorOrNull) {

    IvParameterSpec iv = resolveInitVectorForDecryption(initializationVectorOrNull);

    try {
      Cipher cipher = getCipher(algo);
      SecretKeySpec secret = new SecretKeySpec(cryptoKey.getBytes(), algo.getId());
      cipher.init(Cipher.DECRYPT_MODE, secret, iv);
      return cipher.doFinal(bytes);
    } catch (InvalidKeyException
        | InvalidAlgorithmParameterException
        | IllegalBlockSizeException
        | BadPaddingException e) {
      throw new CryptoEngineException(e);
    }
  }

  @NonNull
  public IvParameterSpec resolveInitVectorForDecryption(
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

  @Override
  public byte[] encrypt(
      byte @NonNull [] unencypted,
      @NonNull CryptoAlgorithm algorithm,
      @NonNull CryptoKey key,
      @NonNull IvParameterSpec initializationVectorForEncryption) {

    try {
      Cipher cipher = getCipher(algorithm);
      SecretKeySpec secret = new SecretKeySpec(key.getBytes(), algorithm.getId());
      cipher.init(Cipher.ENCRYPT_MODE, secret, initializationVectorForEncryption);
      return cipher.doFinal(unencypted);
    } catch (InvalidKeyException
        | InvalidAlgorithmParameterException
        | IllegalBlockSizeException
        | BadPaddingException e) {
      throw new CryptoEngineException(e);
    }
  }

  private Cipher getCipher(@NonNull CryptoAlgorithm defaultAlgorithm) {
    try {
      return Cipher.getInstance(exactCipherNames.get(defaultAlgorithm));
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new CryptoEngineException(e);
    }
  }

  @Override
  public CryptoKey generateKey(@NonNull CryptoAlgorithm algo, @NonNull CryptoKeySize size) {
    try {
      KeyGenerator kgen = KeyGenerator.getInstance(algo.getId());
      kgen.init(size.asInt());
      SecretKey secret = kgen.generateKey();
      return CryptoKey.fromBytes(secret.getEncoded());
    } catch (NoSuchAlgorithmException e) {
      throw new CryptoEngineException(e);
    }
  }

  @Override
  public @NonNull IvParameterSpec getInitVectorForEncryption() {

    if (useRandomInitVector || configuredInitVector == null) {
      byte[] iv = new byte[16];
      RANDOM.nextBytes(iv);
      return new IvParameterSpec(iv);
    }

    if (configuredInitVector == null) {
      throw new IllegalStateException("No init vector configured");
    }
    return configuredInitVector.getIvParameterSpec();
  }
}
