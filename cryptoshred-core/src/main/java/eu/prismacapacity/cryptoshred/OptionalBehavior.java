package eu.prismacapacity.cryptoshred;

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
            return predicate.test(value()) ? Optional.of(value()): Optional.empty();
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
