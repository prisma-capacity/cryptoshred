package eu.prismacapacity.cryptoshred.metrics;

/**
 * Just a Callable<T> without throws clause
 *
 * @author otbe
 * @param <T>
 */
@FunctionalInterface
public interface MetricsCallable<T> {
    T call();
}
