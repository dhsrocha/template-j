package template.feature.user;

import java.util.UUID;
import template.base.contract.CacheManager;

/**
 * User feature cache manager implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
final class UserCache extends CacheManager.Default<User, UUID> {

  @javax.inject.Inject
  UserCache() {
  }

  @Override
  protected Class<UUID> idRef() {
    return UUID.class;
  }

  @Override
  public Class<User> domainRef() {
    return User.class;
  }
}
