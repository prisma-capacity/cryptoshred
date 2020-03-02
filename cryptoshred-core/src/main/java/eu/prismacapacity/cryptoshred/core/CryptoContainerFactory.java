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

import lombok.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.prismacapacity.cryptoshred.core.keys.CryptoKeySize;

public interface CryptoContainerFactory {
	public @NonNull <T> CryptoContainer<T> wrap(@NonNull T t, @NonNull CryptoSubjectId id)
			throws JsonProcessingException;

	public @NonNull <T> CryptoContainer<T> wrap(@NonNull T value, @NonNull CryptoSubjectId id,
			@NonNull CryptoAlgorithm algorithm, @NonNull CryptoKeySize keySize) throws JsonProcessingException;

	public @NonNull <T> CryptoContainer<T> restore(@NonNull Class<T> type, @NonNull CryptoSubjectId id,
			CryptoAlgorithm algo, CryptoKeySize size, byte[] encryptedBytes) throws JsonProcessingException;

}
