package template.base;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.HttpResponseException;
import io.javalin.http.NotFoundResponse;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps application's exceptions.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("ImmutableEnumChecker")
@Slf4j
@AllArgsConstructor
public enum Exceptions implements Supplier<RuntimeException> {
  /**
   * Indicates a general illegal argument.
   */
  ILLEGAL_ARGUMENT(BadRequestResponse::new),
  /**
   * Indicates an empty body is found in a request or response.
   */
  EMPTY_BODY(BadRequestResponse::new),
  /**
   * Indicates an invalid identity is provided.
   */
  INVALID_ID(BadRequestResponse::new),
  /**
   * Indicates a resource that has not been found.
   */
  RESOURCE_NOT_FOUND(NotFoundResponse::new),
  ;

  private final Function<String, HttpResponseException> ex;

  /**
   * Throws a provided exception if at least one of the provided boolean
   * conditions returns {@code true}. It is advisable to provide the computing
   * starting from the least computing cost.
   *
   * @param ex         An {@link RuntimeException} supplied by a string message.
   * @param conditions Indicates an undesirable conditions to trigger the
   *                   supplied exception.
   * @throws HttpResponseException The supplied exception instance.
   * @see #throwIf(String, BooleanSupplier...)
   * @see #throwIf(BooleanSupplier...)
   */
  public static void throwIf(final @NonNull Supplier<RuntimeException> ex,
                             final @NonNull BooleanSupplier... conditions) {
    for (final @NonNull var b : conditions) {
      if (b.getAsBoolean()) {
        throw ex.get();
      }
    }
  }

  /**
   * Throws a provided exception if at least one of the provided boolean
   * conditions returns {@code true}. It is advisable to provide the computing
   * starting from the least computing cost.
   *
   * @param conditions Indicates an undesirable conditions to trigger the
   *                   supplied exception.
   * @throws HttpResponseException The exception indexed by the provided item.
   * @see #throwIf(String, BooleanSupplier...)
   * @see #throwIf(Supplier, BooleanSupplier...)
   */
  public final void throwIf(final @NonNull BooleanSupplier... conditions) {
    throwIf(name(), conditions);
  }

  /**
   * Throws a provided exception if at least one of the provided boolean
   * conditions returns {@code true}. It is advisable to provide the computing
   * starting from the least computing cost.
   *
   * @param message    A custom message to throw along with the indexed
   *                   {@link #ex exception}.
   * @param conditions Indicates an undesirable conditions to trigger the
   *                   supplied exception.
   * @throws HttpResponseException The indexed one by the provided item.
   * @see #throwIf(Supplier, BooleanSupplier...)
   * @see #throwIf(BooleanSupplier...)
   */
  public final void throwIf(final @NonNull String message,
                            final @NonNull BooleanSupplier... conditions) {
    throwIf(() -> ex.apply(message), conditions);
  }

  /**
   * Traps any potential {@link Exception checked exception} thrown out of the
   * provided scoped code and redirect (re-throw) it as the indexed one. It is
   * also a convenient way to write a try-catch block into an one-liner.
   *
   * @param <R>  Type of upcoming result from the provided parameter.
   * @param wrap Potentially throwing wrapped computation.
   * @return Any result supplied in the provided parameter.
   * @throws HttpResponseException The indexed one by the provided item.
   * @see #trapIn(Function)
   * @see #trapIn(CheckedRunnable)
   */
  public final <R> R trapIn(final @NonNull Callable<R> wrap) {
    try {
      return wrap.call();
    } catch (final Exception e) {
      log.error(e.getMessage());
      throw get();
    }
  }

  /**
   * Traps any potential {@link Exception checked exception} thrown out of the
   * provided scoped code and redirect (re-throw) it as the indexed one. It is
   * also a convenient way to write a try-catch block into an one-liner.
   *
   * @param wrap Potentially throwing wrapped computation.
   * @throws HttpResponseException The indexed one by the provided item.
   * @see #trapIn(Function)
   * @see #trapIn(Callable)
   */
  public final <E extends Exception> void trapIn(
      final @NonNull CheckedRunnable<E> wrap) {
    try {
      wrap.wrap();
    } catch (final Exception e) {
      log.error(e.getMessage(), e);
      throw get();
    }
  }

  /**
   * Traps any potential {@link Exception checked exception} thrown out of the
   * provided scoped code and redirect (re-throw) it as the indexed one. It is
   * also a convenient way to write a try-catch block into an one-liner.
   *
   * @param <T> Type provided by the functor.
   * @param <R> Type of upcoming result from the provided parameter.
   * @param fun Potentially throwing wrapped computation.
   * @return Functor result supplied in the provided parameter.
   * @throws HttpResponseException The indexed one by the provided item.
   * @see #trapIn(CheckedRunnable)
   * @see #trapIn(Callable)
   */
  public final <T, R> Function<T, R> trapIn(final @NonNull Function<T, R> fun) {
    return t -> trapIn(() -> fun.apply(t));
  }

  /**
   * Provides the indexed exception with its name as the message.
   *
   * @return An instance of {@link HttpResponseException indexed exception}.
   */
  @Override
  public final HttpResponseException get() {
    return ex.apply(name());
  }

  /**
   * Utility functional interface to suppress a {@link Exception checked
   * exception}.
   *
   * @param <E> Potential exception expected to be thrown.
   */
  @FunctionalInterface
  public interface CheckedRunnable<E extends Exception> {
    void wrap() throws E;
  }
}
