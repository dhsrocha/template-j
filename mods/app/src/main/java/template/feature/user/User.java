package template.feature.user;

import static template.base.Exceptions.INVALID_DOMAIN;

import java.util.Comparator;
import lombok.NonNull;
import lombok.Value;
import template.base.stereotype.Domain;

@Value
public class User implements Domain<User> {

  @NonNull String name;
  int age;

  public static User of(final String name, final int age) {
    final var u = new User(name, age);
    INVALID_DOMAIN.throwIf(IllegalArgumentException::new, ()-> !u.isValid());
    return u;
  }

  @Override
  public final boolean isValid() {
    return !name.isBlank() && age > 0;
  }

  @Override
  public final int compareTo(final @NonNull User user) {
    return Comparator.comparing(User::getAge)
                     .thenComparing(User::getName)
                     .compare(this, user);
  }
}
