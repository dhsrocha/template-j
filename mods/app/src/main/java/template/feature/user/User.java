package template.feature.user;

import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import template.base.contract.CacheManager;
import template.base.contract.Controller;
import template.base.contract.Repository;
import template.base.stereotype.Domain;

import static java.util.Comparator.nullsLast;

/**
 * Domain which represents a user.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@lombok.Value
public class User implements Domain<User> {

  @SuppressWarnings({"ImmutableEnumChecker", "MissingOverride"})
  @Getter
  @AllArgsConstructor
  private enum Rules implements Invariant<User> {
    AGE_ABOVE_ZERO(u -> u.age > 0),
    NAME_NOT_BLANK(u -> null != u && !u.name.isBlank()),
    ;
    private final Predicate<User> test;
  }

  private static final Set<Invariant<User>> SET = Set.of(Rules.values());
  private static final Comparator<User> COMPARATOR = Comparator
      .comparing(User::getAge, nullsLast(Comparator.naturalOrder()))
      .thenComparing(User::getName, nullsLast(Comparator.naturalOrder()));

  @lombok.NonNull String name;
  int age;

  public static User of(final String name, final int age) {
    return Domain.validate(new User(name, age));
  }

  @Override
  public int compareTo(final @lombok.NonNull User user) {
    return COMPARATOR.compare(this, user);
  }

  @Override
  public Set<Invariant<User>> invariants() {
    return SET;
  }

  @SuppressWarnings("unused")
  @dagger.Module
  public interface Mod {

    @dagger.Binds
    CacheManager<User, UUID> cacheManager(final UserCache u);

    @dagger.Binds
    Repository.Cached<User, UUID> repository(final UserRepository u);

    @dagger.Binds
    Controller<User> controller(final UserController u);
  }
}
