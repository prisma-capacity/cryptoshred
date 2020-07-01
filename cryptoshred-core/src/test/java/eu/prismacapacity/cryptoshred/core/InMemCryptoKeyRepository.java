package eu.prismacapacity.cryptoshred.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import eu.prismacapacity.cryptoshred.core.keys.CryptoKey;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyNotFoundAfterCreatingException;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeyRepository;
import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;

public class InMemCryptoKeyRepository implements CryptoKeyRepository {

	final Map<CryptoSubjectId, CryptoKey> keys = new HashMap<>();
	private final CryptoEngine engine;

	public InMemCryptoKeyRepository(CryptoEngine engine) {
		this.engine = engine;
	}

	@Override
	public synchronized Optional<CryptoKey> findKeyFor(CryptoSubjectId subjectId, CryptoAlgorithm algo,
			CryptoKeySize size) {
		return Optional.ofNullable(keys.get(subjectId));
	}

	@Override
	public synchronized CryptoKey getOrCreateKeyFor(CryptoSubjectId subjectId, CryptoAlgorithm algo, CryptoKeySize size)
			throws CryptoKeyNotFoundAfterCreatingException {
		Optional<CryptoKey> existingKey = findKeyFor(subjectId, algo, size);
		return existingKey.orElseGet(() -> {
			CryptoKey key;

			key = engine.generateKey(algo, size);
			keys.put(subjectId, key);
			return key;
		});
	}

}
