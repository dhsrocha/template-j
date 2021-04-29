package template.base;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.NotFoundResponse;
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
   * Indicates a resource that has not been found.
   */
  RESOURCE_NOT_FOUND(NotFoundResponse::new),
  ;

  private final Function<String, RuntimeException> ex;

  /**
   * Throws a provided exception if at least one of the provided boolean
   * conditions returns true. It is advisable to provide the computing
   * starting from the least computing cost.
   *
   * @param ex         An {@link RuntimeException} supplied by a string message.
   * @param conditions Indicates an undesirable conditions to trigger the
   *                   supplied exception.
   * @throws RuntimeException The supplied exception instance.
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
   * conditions returns true. It is advisable to provide the computing
   * starting from the least computing cost.
   *
   * @param conditions Indicates an undesirable conditions to trigger the
   *                   supplied exception.
   * @throws RuntimeException The exception indexed by the provided item.
   * @see #throwIf(String, BooleanSupplier...)
   */
  public final void throwIf(final @NonNull BooleanSupplier... conditions) {
    throwIf(name(), conditions);
  }

  /**
   * Throws a provided exception if at least one of the provided boolean
   * conditions returns true. It is advisable to provide the computing
   * starting from the least computing cost.
   *
   * @param message    A custom message to throw along with the indexed
   *                   {@link #ex exception}.
   * @param conditions Indicates an undesirable conditions to trigger the
   *                   supplied exception.
   * @throws RuntimeException The exception indexed by the provided item.
   * @see #throwIf(Supplier, BooleanSupplier...)
   */
  public final void throwIf(final @NonNull String message,
                            final @NonNull BooleanSupplier... conditions) {
    throwIf(() -> ex.apply(message), conditions);
  }

  /**
   * Traps any potential {@link RuntimeException} thrown out of the provided
   * scoped code and redirect (re-throw) it as the indexed one.
   *
   * @param <R>   Type of upcoming result from the provided parameter.
   * @param scope Computation where a {@link RuntimeException} might be thrown
   *              from.
   * @return Any result supplied in the provided parameter.
   */
  public final <R> R trapIn(final @NonNull Supplier<R> scope) {
    try {
      return scope.get();
    } catch (final RuntimeException e) {
      log.error(e.getMessage());
      throw get();
    }
  }

  /**
   * Provides the indexed exception with its name as the message.
   *
   * @return An instance of indexed exception.
   */
  @Override
  public final RuntimeException get() {
    return ex.apply(name());
  }
}
