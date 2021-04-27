package template.feature.user;

import static java.util.Comparator.nullsLast;

import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import template.base.Checks;
import template.base.contract.CacheManager;
import template.base.contract.Controller;
import template.base.contract.Repository;
import template.base.contract.Routes;
import template.base.stereotype.Domain;

/**
 * Domain which represents a user.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@lombok.Value
public class User implements Domain<User> {

  /**
   * {@link User} business rules.
   */
  @SuppressWarnings({"ImmutableEnumChecker", "MissingOverride"})
  @Getter
  @AllArgsConstructor
  private enum Rules implements Invariant<User> {
    AGE_ABOVE_ZERO(u -> u.age > 0),
    NAME_NOT_BLANK(Checks.notBlank(User::getName)),
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

  /**
   * Type for binding package-private implementations to public interfaces.
   * It is meant to be included into a {@link Routes} managed module.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
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
