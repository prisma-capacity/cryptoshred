package eu.prismacapacity.cryptoshred.core;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;
import lombok.NonNull;

public interface CryptoContainerFactory {
	public @NonNull <T> CryptoContainer<T> wrap(@NonNull T t, @NonNull CryptoSubjectId id)
			throws JsonProcessingException;

	public @NonNull <T> CryptoContainer<T> wrap(@NonNull T value, @NonNull CryptoSubjectId id,
			@NonNull CryptoAlgorithm algorithm, @NonNull CryptoKeySize keySize) throws JsonProcessingException;

}
