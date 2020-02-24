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

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

abstract class OptionalBehavior<T> {

	abstract T value();

	////////////////////////////////////////
	// stolen from optional
	////////////////////////////////////////

	public T get() {
		if (value() == null) {
			throw new NoSuchElementException("No value present");
		}
		return value();
	}

	public boolean isPresent() {
		return value() != null;
	}

	public void ifPresent(Consumer<? super T> consumer) {
		if (value() != null)
			consumer.accept(value());
	}

	public Optional<T> filter(Predicate<? super T> predicate) {
		Objects.requireNonNull(predicate);
		if (!isPresent())
			return Optional.empty();
		else
			return predicate.test(value()) ? Optional.of(value()) : Optional.empty();
	}

	public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
		Objects.requireNonNull(mapper);
		if (!isPresent())
			return Optional.empty();
		else {
			return Optional.ofNullable(mapper.apply(value()));
		}
	}

	public <U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
		Objects.requireNonNull(mapper);
		if (!isPresent())
			return Optional.empty();
		else {
			return Objects.requireNonNull(mapper.apply(value()));
		}
	}

	public T orElse(T other) {
		return value() != null ? value() : other;
	}

	public T orElseGet(Supplier<? extends T> other) {
		return value() != null ? value() : other.get();
	}

	public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		if (value() != null) {
			return value();
		} else {
			throw exceptionSupplier.get();
		}
	}
}
