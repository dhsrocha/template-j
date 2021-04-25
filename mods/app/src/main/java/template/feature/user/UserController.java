package template.feature.user;

import java.util.UUID;
import java.util.function.Predicate;
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
  UserController(final @lombok.NonNull CacheManager<User, UUID> cache,
                 final @lombok.NonNull Repository.Cached<User, UUID> repo) {
    super(cache, repo);
  }

  @Override
  public Class<User> domainRef() {
    return User.class;
  }

  @Override
  public Predicate<User> filter(final @lombok.NonNull User c) {
    return super.filter(c)
                .or(u -> u.getAge() == c.getAge())
                .or(u -> u.getName().equals(c.getName()));
  }
}
