package template.feature.user;

import java.util.UUID;
import lombok.NonNull;
import template.base.contract.CacheManager;
import template.base.contract.Controller;
import template.base.contract.Repository;

/**
 * User feature controller implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
final class UserController extends Controller.Cached<User> {

  @javax.inject.Inject
  UserController(final @NonNull CacheManager<User, UUID> cache,
                 final @NonNull Repository.Cached<User, UUID> repo) {
    super(cache, repo);
  }

  @Override
  public Class<User> domainRef() {
    return User.class;
  }
}
