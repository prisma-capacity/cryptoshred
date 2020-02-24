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
package eu.prismacapacity.cryptoshred.core.keys;

import java.util.Base64;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class CryptoKey {
	@Getter
	@NonNull
	private final String base64;
	@Getter
	@NonNull
	private final byte[] bytes;

	public static CryptoKey fromBase64(@NonNull String base64encoded) {
		return new CryptoKey(base64encoded, Base64.getDecoder().decode(base64encoded));
	}

	public static CryptoKey fromBytes(@NonNull byte[] bytes) {
		return new CryptoKey(Base64.getEncoder().encodeToString(bytes), bytes);
	}

	@Override
	public String toString() {
		return getBase64();
	}

}
