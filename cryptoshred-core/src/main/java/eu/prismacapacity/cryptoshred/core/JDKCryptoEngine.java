/*
 * Copyright © 2020-2026 PRISMA European Capacity Platform GmbH
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
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import lombok.NonNull;

public class JDKCryptoEngine extends AbstractCryptoEngine {

  public JDKCryptoEngine(String configuredInitVectorOrNull, boolean useRandomInitVector) {
    super(configuredInitVectorOrNull, useRandomInitVector);
  }

  @Override
  public byte[] decrypt(
      @NonNull CryptoAlgorithm algo,
      @NonNull CryptoKey cryptoKey,
      byte @NonNull [] bytes,
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
  @NonNull
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

}
