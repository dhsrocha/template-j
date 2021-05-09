package template.feature.user;

import static java.util.Comparator.nullsLast;

import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import template.base.Checks;
import template.base.contract.CacheManager;
import template.base.contract.Controller;
import template.base.contract.Controller.Aggregate;
import template.base.contract.Repository;
import template.base.contract.Router;
import template.base.stereotype.Domain;
import template.feature.address.Address;

/**
 * {@link Domain} which represents an user.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Schema
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
    USERNAME_NOT_BLANK(Checks.NOT_BLANK.on(User::getUsername)),
    NAME_NOT_BLANK(Checks.NOT_BLANK.on(User::getName)),
    EMAIL_INVALID(Checks.VALID_EMAIL.on(User::getEmail)),
    ;
    private final Predicate<User> test;
  }

  private static final Set<Invariant<User>> SET = Set.of(Rules.values());
  private static final Comparator<User> COMPARATOR = Comparator
      .comparing(User::getUsername, nullsLast(Comparator.naturalOrder()))
      .thenComparing(User::getEmail, nullsLast(Comparator.naturalOrder()))
      .thenComparing(User::getName, nullsLast(Comparator.naturalOrder()))
      .thenComparing(User::getAge, nullsLast(Comparator.naturalOrder()));

  @lombok.NonNull String username;
  @lombok.NonNull String email;
  @lombok.NonNull String name;
  int age;

  public static User of(final String username, final String email,
                        final String name, final int age) {
    return Domain.validate(new User(username, email, name, age));
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
   * It is meant to be included into a {@link Router} managed module.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @SuppressWarnings("unused")
  @dagger.Module
  public interface Mod {

    // Controller

    @dagger.Binds
    Controller<User> controller(final UserController u);

    @dagger.Binds
    Aggregate<User, Address> withAddress(final UserController.WithAddress a);

    // Repository

    @dagger.Binds
    Repository<User, UUID> repo(final UserRepository r);

    @dagger.Binds
    Repository.Cached<User, UUID> repoCached(final UserRepository r);

    @dagger.Binds
    Repository.Composable<User, Address, UUID> repoAddress(
        final UserRepository.WithAddress a);

    // Caching

    @dagger.Binds
    CacheManager<User, UUID> cache(final UserCache c);
  }
}
