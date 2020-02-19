package eu.prismacapacity.cryptoshred.core.keys;

import java.util.Optional;

import eu.prismacapacity.cryptoshred.core.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.core.CryptoSubjectId;
import lombok.NonNull;

public interface CryptoKeyRepository {

	public @NonNull Optional<CryptoKey> findKeyFor(@NonNull CryptoSubjectId subjectId, @NonNull CryptoAlgorithm algo,
			@NonNull CryptoKeySize size);

	public @NonNull CryptoKey getOrCreateKeyFor(@NonNull CryptoSubjectId subjectId, @NonNull CryptoAlgorithm algo,
			@NonNull CryptoKeySize size) throws CryptoKeyNotFoundAfterCreatingException;

}
