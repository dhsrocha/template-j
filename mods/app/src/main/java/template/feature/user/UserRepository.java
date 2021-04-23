package template.feature.user;

import java.util.HashMap;
import template.base.contract.Repository;

/**
 * User feature repository implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
final class UserRepository extends Repository.Default<User> {

  @javax.inject.Inject
  UserRepository() {
    super(new HashMap<>());
  }
}
