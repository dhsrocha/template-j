package template.base;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Maps application's exceptions.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("ImmutableEnumChecker")
@AllArgsConstructor
public enum Exceptions {
  /**
   * Indicates a general illegal argument.
   */
  ILLEGAL_ARGUMENT(IllegalArgumentException::new),
  ;

  private final Function<String, RuntimeException> ex;

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
}
