package template.base;

import java.util.function.Function;
import java.util.function.Predicate;
import lombok.NonNull;

/**
 * Value object for text fields. The purpose is to ensemble.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Checks {

  static <T> Predicate<T> notBlank(final @NonNull Function<T, String> get) {
    return t -> null != t && null != get.apply(t) && !get.apply(t).isBlank();
  }

  static <T> Predicate<T> enumNotNull(final @NonNull Function<T, Enum<?>> get) {
    return t -> null != t && null != get.apply(t);
  }
}
