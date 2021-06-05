package template.base;

import io.javalin.http.BadRequestResponse;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import template.base.stereotype.Domain;

/**
 * General validation logic codex meant to apply across {@link Domain} types'
 * indexed business rules.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("ImmutableEnumChecker")
@AllArgsConstructor
public enum Checks {
  NOT_BLANK(String.class, t -> !((String) t).isBlank()),
  VALID_EMAIL(String.class, NOT_BLANK.test
      .and(t -> Patterns.EMAIL.matcher((String) t).matches())),
  ;
  private final Class<?> ref;
  private final Predicate<Object> test;

  /**
   * Applies the indexed validation logic <i>on</i> the value returned by the
   * provided functor parameter it is not null and shares type with the indexed
   * one.
   *
   * @param getter Provides a value according to a {@link Domain} attribute.
   * @param <T>    Input type that is used by the returning {@link Predicate}.
   * @return if all pre-requirements and indexed validation successfully pass.
   * @throws BadRequestResponse if any exception is thrown occurs during
   *                            validation on provided value from parameter.
   */
  public final <T> Predicate<T> on(final @NonNull Function<T, ?> getter) {
    return Exceptions.ILLEGAL_ARGUMENT.trapIn(() -> t -> {
      final var r = getter.apply(t);
      return null != r && r.getClass().equals(ref) && test.test(r);
    });
  }

  /**
   * Utility class to define patterns to be used in {@link Checks}' indexes.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  private static final class Patterns {

    private static final String SUB_DOMAIN = ""
        + "^[a-z0-9.!#$%&'*+/=?^_`{|}~-]+@"
        + "[a-z0-9][a-z0-9-]{0,61}[a-z0-9]?"
        + "\\.[a-z0-9][a-z0-9-]{0,61}[a-z0-9]?"
        + "(?:\\.[a-z0-9][a-z0-9-]{0,61}[a-z0-9])?"
        + "$";

    /**
     * Email pattern specified by WHATWG.
     *
     * @see
     * <a href="https://html.spec.whatwg.org/multipage/input.html#valid-e-mail-address" />
     */
    private static final Pattern EMAIL = Pattern.compile(
        SUB_DOMAIN, Pattern.CASE_INSENSITIVE);
  }
}
