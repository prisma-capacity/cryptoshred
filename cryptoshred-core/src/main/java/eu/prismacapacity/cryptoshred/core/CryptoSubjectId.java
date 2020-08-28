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

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class CryptoSubjectId {

    @NonNull
    private final Supplier<UUID> supplier;

    public UUID getId() {
        return Objects.requireNonNull(supplier.get());
    }

    public static CryptoSubjectId of(@NonNull UUID id) {
        return of(() -> id);
    }

    public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof CryptoSubjectId)) {
			return false;
		}
        final CryptoSubjectId other = (CryptoSubjectId) o;
        return Objects.equals(this.getId(), other.getId());
    }

    public int hashCode() {
        return supplier.get().hashCode();
    }

    // might trigger premature get()
    // public String toString() { return "CryptoSubjectId(id=" + this.getId() + ")"; }
}
