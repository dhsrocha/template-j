package template.feature.user;

import java.util.Comparator;
import lombok.NonNull;
import lombok.Value;
import template.application.Exceptions;

@Value
public class User implements Comparable<User> {

  @NonNull String name;
  int age;

  static User of(final String name, final int age) {
    Exceptions.INVALID_DOMAIN.throwIf(IllegalArgumentException::new,
                                      name::isBlank, () -> age <= 0);
    return new User(name, age);
  }

  @Override
  public final int compareTo(final @NonNull User user) {
    return Comparator.comparing(User::getAge)
                     .thenComparing(User::getName)
                     .compare(this, user);
  }
}
