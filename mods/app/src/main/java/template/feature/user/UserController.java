package template.feature.user;

import java.util.UUID;
import template.base.contract.CacheManager;
import template.base.contract.Controller;
import template.base.contract.Repository;
import template.base.contract.Service;
import template.feature.address.Address;
import template.feature.auth.Auth;
import template.feature.auth.Auth.Role;

/**
 * {@link User} feature controller implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
final class UserController extends Service.Cached<User, UUID>
    implements Controller<User> {

  private final Service<Auth, UUID> auth;

  @javax.inject.Inject
  UserController(final @lombok.NonNull CacheManager<User, UUID> cache,
                 final @lombok.NonNull Repository.Cached<User, UUID> repo,
                 final @lombok.NonNull Service<Auth, UUID> auth) {
    super(cache, repo);
    this.auth = auth;
  }

  @Override
  public Class<User> ref() {
    return User.class;
  }

  @Override
  public UUID create(final @lombok.NonNull User u) {
    final var uuid = super.create(u);
    auth.create(Auth.of(uuid, Role.DEFAULT));
    return uuid;
  }

  /**
   * Aggregate implementation between {@link User} and {@link Address} domains.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  static final class WithAddress
      extends Service.Composed<User, Address, UUID>
      implements Controller.Aggregate<User, Address> {

    @javax.inject.Inject
    WithAddress(
        final @lombok.NonNull Repository.Composable<User, Address, UUID> base) {
      super(base);
    }

    @Override
    public Class<User> ref() {
      return User.class;
    }

    @Override
    public Class<Address> extRef() {
      return Address.class;
    }
  }
}
