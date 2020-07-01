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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;

@RequiredArgsConstructor
public class JDKCryptoEngine implements CryptoEngine {

	private Map<CryptoAlgorithm, String> exactCipherNames = createExactCipherMapping();

	private static Map<CryptoAlgorithm, String> createExactCipherMapping() {
		// initialize with known algorithms
		HashMap<CryptoAlgorithm, String> map = new HashMap<>();
		map.put(CryptoAlgorithm.AES_CBC, "AES/CBC/PKCS5PADDING");
		return Collections.unmodifiableMap(map);
	}

	@NonNull
	private final CryptoInitializationVector initVector;

	@Override
	public byte[] decrypt(@NonNull CryptoAlgorithm algo, @NonNull CryptoKey cryptoKey, @NonNull byte[] bytes) {
		IvParameterSpec iv = new IvParameterSpec(initVector.getBytes());
		try {
			Cipher cipher = getCipher(algo);
			SecretKeySpec secret = new SecretKeySpec(cryptoKey.getBytes(), algo.getId());
			cipher.init(Cipher.DECRYPT_MODE, secret, iv);
			return cipher.doFinal(bytes);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw new CryptoEngineException(e);
		}
	}

	@Override
	public byte[] encrypt(@NonNull byte[] unencypted, @NonNull CryptoAlgorithm algorithm, @NonNull CryptoKey key,
			@NonNull CryptoObjectMapper mapper) {

		IvParameterSpec iv = new IvParameterSpec(initVector.getBytes());
		try {
			Cipher cipher = getCipher(algorithm);
			SecretKeySpec secret = new SecretKeySpec(key.getBytes(), algorithm.getId());
			cipher.init(Cipher.ENCRYPT_MODE, secret, iv);
			return cipher.doFinal(unencypted);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
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

}
