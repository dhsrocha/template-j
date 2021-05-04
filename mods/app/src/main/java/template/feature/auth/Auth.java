package template.feature.auth;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.NonNull;
import template.base.contract.Controller;
import template.base.contract.Filter;
import template.base.contract.Router;
import template.base.stereotype.Domain;
import template.feature.user.User;

/**
 * Ensembles system's authentication and authorization concerns.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@lombok.Value
public class Auth implements Domain<Auth> {

  /**
   * {@link Auth} business rules.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @SuppressWarnings({"ImmutableEnumChecker", "MissingOverride"})
  @lombok.Getter
  @lombok.AllArgsConstructor
  enum Rules implements Invariant<Auth> {
    ;
    private final Predicate<Auth> test;
  }

  /**
   * Describes access levels for a {@link User}.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  public enum Role {
    DEFAULT, ADMIN,
  }

  /**
   * Describes states of activation for as {@link Auth} registry.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  public enum State {
    INACTIVE, ACTIVE, ON_CONFIRM,
  }

  private static final Comparator<Auth> COMPARATOR = Comparator
      .comparing(Auth::getRole)
      .thenComparing(Auth::getUserId);

  UUID userId;
  Role role;

  public static Auth of(final @lombok.NonNull UUID usedId,
                        final @lombok.NonNull Role role) {
    return Domain.validate(new Auth(usedId, role));
  }

  @Override
  public Set<Invariant<Auth>> invariants() {
    return Collections.emptySet();
  }

  @Override
  public int compareTo(final @NonNull Auth a) {
    return COMPARATOR.compare(this, a);
  }

  Token authorize(final @NonNull User u) {
    // TODO Authorization
    //  * encode password
    //  * compare with database
    //  * evaluate success
    //  * return generated token
    return null;
  }

  boolean register(final @NonNull User u) {
    // TODO Account creation
    //  * Create account at user creation under 2FA logic
    //  * Call Service<Auth,UUID>#create() at user creation
    //  * Create one-way state process for representing state of activation
    return false;
  }

  boolean acceptNew(final @NonNull User u) {
    // TODO:
    //  * expose endpoint for accepting 2FA reply (upcoming update will
    //    depend on email sending)
    return false;
  }

  boolean authenticate(final @NonNull Token t) {
    // TODO Authentication
    //  * evaluate:
    //    * claims (roles, other entries)
    //    * expiration token
    //  * (other rules associated to it)
    //  * if successful:
    //    * refresh expiration time
    //    * compact token into string
    //    * return it as header
    return false;
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

    @dagger.Binds
    Filter<Auth> filter(final @lombok.NonNull AuthController f);

    @dagger.Binds
    Controller.Single<Auth> getter(final @lombok.NonNull AuthController g);
  }
}
