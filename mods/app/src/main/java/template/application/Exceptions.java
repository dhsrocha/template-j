package template.application;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Maps application's exception.
 */
@AllArgsConstructor
public enum Exceptions {
  /**
   * Indicates that a domain object received invalid parameters.
   */
  INVALID_DOMAIN,
  ;

  /**
   * Throws a provided exception if at least one of the provided boolean
   * conditions returns true. It is advisable to provide the computing
   * starting from the least computing cost.
   *
   * @param e          An {@link RuntimeException} supplied by a string message.
   * @param conditions Indicates an undesirable conditions to trigger the
   *                   supplied exception.
   */
  public final void throwIf(final @NonNull Function<String, RuntimeException> e,
                            final @NonNull BooleanSupplier... conditions) {
    for (final @NonNull var b : conditions) {
      if (b.getAsBoolean()) {
        throw e.apply(name());
      }
    }
  }
}
