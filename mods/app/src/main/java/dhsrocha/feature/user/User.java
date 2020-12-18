package dhsrocha.feature.user;

import lombok.Value;

@Value
class User {

  String name;
  int age;

  static User of(final String name, final int age) {
    if (null == name || "".equals(name)) {
      throw new IllegalArgumentException();
    }
    if (age <= 0) {
      throw new IllegalArgumentException();
    }
    return new User(name, age);
  }
}
