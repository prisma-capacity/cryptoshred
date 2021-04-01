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

import java.io.IOException;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyRepository;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import eu.prismacapacity.cryptoshred.core.metrics.CryptoMetrics;

@Slf4j
public class CryptoContainer<T> extends OptionalBehavior<T> {

	public CryptoContainer(@NonNull T value, @NonNull CryptoSubjectId subjectId) {
		this(value, subjectId, CryptoAlgorithm.AES_CBC, CryptoKeySize.BIT_256);
	}

	public CryptoContainer(@NonNull T value, @NonNull CryptoSubjectId subjectId, @NonNull CryptoAlgorithm algo,
			@NonNull CryptoKeySize size) {
		this.cachedValue = Optional.ofNullable(value);
		this.type = value.getClass();
		this.subjectId = subjectId;
		this.algo = algo;
		this.size = size;
	}

	private CryptoContainer(Class<T> targetType, CryptoAlgorithm algo, CryptoKeySize size, CryptoSubjectId id,
			byte[] encrypted, CryptoEngine engine, CryptoKeyRepository keyRepo, CryptoMetrics metrics,
			ObjectMapper om) {
		this.cachedValue = null;
		this.type = targetType;
		this.algo = algo;
		this.size = size;
		this.subjectId = id;
		this.engine = engine;
		this.keyRepo = keyRepo;
		this.metrics = metrics;
		this.mapper = om;
		this.encryptedBytes = encrypted;
	}

	// set only on deserialization
	private transient CryptoEngine engine;
	private transient CryptoKeyRepository keyRepo;
	private transient CryptoMetrics metrics;
	private transient ObjectMapper mapper;

	static <T> CryptoContainer<T> fromDeserialization(Class<T> targetType, CryptoAlgorithm algorithm,
			CryptoKeySize keySize, CryptoSubjectId subjectId, byte[] encrypted, CryptoEngine engine,
			CryptoKeyRepository keyRepo, CryptoMetrics metrics, ObjectMapper om) {
		return new CryptoContainer<T>(targetType, algorithm, keySize, subjectId, encrypted, engine, keyRepo, metrics,
				om);
	}

	@Getter
	private Class<?> type;

	@Getter
	private CryptoAlgorithm algo;

	@Getter
	private CryptoKeySize size;

	@Getter
	private CryptoSubjectId subjectId;

	// the encrypted value
	@Getter(value = AccessLevel.PACKAGE)
	private byte[] encryptedBytes;

	// set after decryption or before encryption for short circuit retrieval
	private transient Optional<T> cachedValue;

	@Override
	protected T value() {
		if (cachedValue == null) {
			cachedValue = Optional.ofNullable(decrypt());
		}
		return cachedValue.orElse(null);
	}

	private T decrypt() {
		byte[] bytes = encryptedBytes;
		if (bytes != null) {
			Optional<CryptoKey> key = keyRepo.findKeyFor(getSubjectId(), getAlgo(), getSize());

			if (key.isPresent()) {

				byte[] decrypted = engine.decrypt(getAlgo(), key.get(), bytes);
				try {
					T t = mapper.readerFor(getType()).readValue(decrypted);
  				if (metrics != null) metrics.notifyDecryptionSuccess();
					return t;
				} catch (IOException e) {
					metrics.notifyDecryptionFailure(e);
					log.warn("Exception while decryption", e);
				}
			} else {
				// key missing, nothing to see here...
				if (metrics != null) metrics.notifyMissingKey();
			}

			// no value, nothing to do here...
		}
		return null;
	}

	@SneakyThrows
	protected void encrypt(CryptoKeyRepository keyRepository, CryptoEngine engine, ObjectMapper om) {
		CryptoKey key = keyRepository.getOrCreateKeyFor(subjectId, algo, size);
		byte[] bytes;
		bytes = om.writeValueAsBytes(value());
		this.encryptedBytes = engine.encrypt(bytes, algo, key);
	}
}
