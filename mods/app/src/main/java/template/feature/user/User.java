package template.feature.user;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import lombok.NonNull;
import lombok.Value;
import template.base.stereotype.Domain;

@Value
public class User implements Domain<User> {

  private enum Rules implements Invariant {
    AGE_ABOVE_ZERO, NAME_NOT_BLANK
  }

  private static final Map<Invariant, Function<User, Boolean>>
      RULES = Map.of(Rules.AGE_ABOVE_ZERO, u -> u.age > 0,
                     Rules.NAME_NOT_BLANK, u -> !u.name.isBlank());

  @NonNull String name;
  int age;

  public static User of(final String name, final int age) {
    return Domain.validate(new User(name, age));
  }

  @Override
  public Map<Invariant, Function<User, Boolean>> invariants() {
    return RULES;
  }

  @Override
  public int compareTo(final @NonNull User user) {
    return Comparator.comparing(User::getAge)
                     .thenComparing(User::getName)
                     .compare(this, user);
  }
}
