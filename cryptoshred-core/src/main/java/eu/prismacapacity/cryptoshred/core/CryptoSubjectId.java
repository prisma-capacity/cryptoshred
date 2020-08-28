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

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class CryptoSubjectId {

	private CryptoSubjectId() {}

	public static CryptoSubjectId of(@NonNull UUID id) {
		return new CryptoSubjectId() {
			@Override
			public @NonNull UUID getId() {
				return id;
			}
		};
	}

	public static CryptoSubjectId of(@NonNull Supplier<UUID> supplier) {
		return new CryptoSubjectId() {
			@Override
			public @NonNull UUID getId() {
				return Objects.requireNonNull(supplier.get());
			}
		};
	}

	@NonNull
	public abstract UUID getId();

	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof CryptoSubjectId)) return false;
		final CryptoSubjectId other = (CryptoSubjectId) o;
		return Objects.equals(this.getId(), other.getId());
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		return result;
	}

	public String toString() {
		return "CryptoSubjectId(id=" + this.getId() + ")";
	}
}
