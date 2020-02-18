package eu.prismacapacity.cryptoshred.keys;

import java.util.Optional;

import eu.prismacapacity.cryptoshred.CryptoAlgorithm;
import eu.prismacapacity.cryptoshred.CryptoSubjectId;
import lombok.NonNull;

public interface CryptoKeyRepository {

	public @NonNull Optional<CryptoKey> findKeyFor(@NonNull CryptoSubjectId subjectId, @NonNull CryptoAlgorithm algo,
			@NonNull CryptoKeySize size);

	public @NonNull CryptoKey getOrCreateKeyFor(@NonNull CryptoSubjectId subjectId, @NonNull CryptoAlgorithm algo,
			@NonNull CryptoKeySize size) throws CryptoKeyNotFoundAfterCreatingException;

}
