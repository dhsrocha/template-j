package template.feature.user;

import java.util.UUID;
import template.base.contract.Dao;
import template.base.contract.Repository;
import template.feature.address.Address;

/**
 * {@link User} feature repository implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
final class UserRepository extends Repository.Default<User>
    implements Repository.Cached<User, UUID> {

  @javax.inject.Inject
  UserRepository(final @lombok.NonNull Dao dao) {
    super(dao, User.class);
  }

  /**
   * User feature with Address aggregation repository implementation.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  static final class WithAddress
      extends Repository.Composed<User, Address>
      implements Repository.Composable<User, Address, UUID> {

    @javax.inject.Inject
    WithAddress(final @lombok.NonNull Repository<User, UUID> repo,
                final @lombok.NonNull Dao dao) {
      super(repo, dao);
    }

    @Override
    public Class<User> ref() {
      return User.class;
    }

    @Override
    protected Class<Address> extRef() {
      return Address.class;
    }
  }
}
