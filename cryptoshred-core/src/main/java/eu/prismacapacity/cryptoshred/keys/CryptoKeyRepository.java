package eu.prismacapacity.cryptoshred.keys;

import java.util.Optional;

import eu.prismacapacity.cryptoshred.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.CryptoSubjectId;

public interface CryptoKeyRepository {

	public Optional<CryptoKey> findKeyFor(CryptoSubjectId subjectId, CryptoAlgorithm algo, CryptoKeySize size);

	public CryptoKey getOrCreateKeyFor(CryptoSubjectId subjectId, CryptoAlgorithm algo, CryptoKeySize size)
			throws CryptoKeyNotFoundAfterCreatingException;

}
