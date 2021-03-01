package template.feature.user;

import lombok.NonNull;
import lombok.Value;
import template.application.Exceptions;

@Value
class User {

  @NonNull String name;
  int age;

  static User of(final String name, final int age) {
    Exceptions.INVALID_DOMAIN.throwIf(IllegalArgumentException::new,
                                      name::isBlank, () -> age <= 0);
    return new User(name, age);
  }
}
