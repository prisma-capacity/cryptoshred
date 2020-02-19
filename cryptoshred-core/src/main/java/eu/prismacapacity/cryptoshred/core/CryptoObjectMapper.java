package eu.prismacapacity.cryptoshred.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyRepository;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import eu.prismacapacity.cryptoshred.core.metrics.CryptoMetrics;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class CryptoObjectMapper implements CryptoContainerFactory {

    static final String JACKSON_INJECT_NAME = "cryptoshredding.CryptoObjectMapper";

    @NonNull
    final CryptoKeyRepository keyRepository;

    @NonNull
    final CryptoMetrics metrics;

    @NonNull
    final CryptoAlgorithm defaultAlgorithm;

    @NonNull
    final CryptoKeySize defaultKeySize;

    @NonNull
    final CryptoEngine engine;

    @NonNull
    @lombok.experimental.Delegate
    final ObjectMapper mapper;

    @Override
    public final @NonNull <T> CryptoContainer<T> wrap(@NonNull T value, @NonNull CryptoSubjectId id)
            throws JsonProcessingException {
        return wrap(value, id, defaultAlgorithm, defaultKeySize);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <T> CryptoContainer<T> wrap(@NonNull T value, @NonNull CryptoSubjectId id,
            @NonNull CryptoAlgorithm algorithm, @NonNull CryptoKeySize keySize)
            throws JsonProcessingException {

        CryptoKey key = keyRepository.getOrCreateKeyFor(id, algorithm, keySize);
        byte[] bytes;
        bytes = writeValueAsBytes(value);
        byte[] encryptToBytes = engine.encrypt(bytes, algorithm, key, this);

        return CryptoContainer.fromValue((Class<T>) value.getClass(), algorithm, keySize, id,
                encryptToBytes, value,
                this);

    }

    <T> T unwrap(@NonNull CryptoContainer<T> cryptoContainer) {
		byte[] bytes = cryptoContainer.getEncryptedBytes();
		if (bytes != null) {
			Optional<CryptoKey> key = keyRepository.findKeyFor(cryptoContainer.getSubjectId(),
					cryptoContainer.getAlgo(), cryptoContainer.getSize());

			if (key.isPresent()) {

				byte[] decrypted = engine.decrypt(cryptoContainer.getAlgo(), key.get(), bytes);
				try {
					T t = readerFor(cryptoContainer.getType()).readValue(decrypted);
					metrics.notifyDecryptionSuccess();
					return t;
				} catch (IOException e) {
					metrics.notifyDecryptionFailure(e);
				}
			} else {
				// key missing, nothing to see here...
				metrics.notifyMissingKey();
			}
		
			// no value, nothing to do here...
		}
		return null;
	}

	@Accessors(chain = true, fluent = true)
	@Data
	@RequiredArgsConstructor
	static class Builder {
		@NonNull
		final CryptoKeyRepository repository;
		@NonNull
		final CryptoEngine engine;

		ObjectMapper mapper = null;
		CryptoMetrics metrics = new CryptoMetrics.NOP();
		CryptoAlgorithm defaultAlgo = CryptoAlgorithm.AES_CBC;
		CryptoKeySize defaultKeySize = CryptoKeySize.BIT_256;

		public CryptoObjectMapper build() {
			CryptoObjectMapper com = new CryptoObjectMapper(repository, metrics, defaultAlgo, defaultKeySize, engine,
					mapper != null ? mapper : new ObjectMapper());

			HashMap<String, Object> hashMap = new HashMap<>();
			hashMap.put(JACKSON_INJECT_NAME, com);
			com.setInjectableValues(new InjectableValues.Std(hashMap));

			return com;
		}
	}

    public static Builder builder(@NonNull CryptoKeyRepository repository,
            @NonNull CryptoEngine engine) {
        return new Builder(repository, engine);
    }

}
