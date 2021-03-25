package template.feature.user;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import template.base.stereotype.Domain;

/**
 * Domain which represents a user.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@lombok.Value
public class User implements Domain<User> {

  private enum Rules implements Invariant {
    /**
     * {@link User#age} should be above zero.
     */
    AGE_ABOVE_ZERO,
    /**
     * {@link User#name} should be non-blank.
     */
    NAME_NOT_BLANK
  }

  private static final Map<Invariant, Function<User, Boolean>>
      RULES = Map.of(Rules.AGE_ABOVE_ZERO, u -> u.age > 0,
                     Rules.NAME_NOT_BLANK, u -> !u.name.isBlank());

  @lombok.NonNull String name;
  int age;

  public static User of(final String name, final int age) {
    return Domain.validate(new User(name, age));
  }

  @Override
  public Map<Invariant, Function<User, Boolean>> invariants() {
    return RULES;
  }

  @Override
  public int compareTo(final @lombok.NonNull User user) {
    return Comparator.comparing(User::getAge)
                     .thenComparing(User::getName)
                     .compare(this, user);
  }
}
