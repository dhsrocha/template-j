package template.application;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public enum Exceptions {
  INVALID_DOMAIN,
  ;

  public final void throwIf(final @NonNull Function<String, RuntimeException> e,
                            final @NonNull BooleanSupplier... conditions) {
    for (final @NonNull var b : conditions) {
      if (b.getAsBoolean()) {
        throw e.apply(name());
      }
    }
  }
}
