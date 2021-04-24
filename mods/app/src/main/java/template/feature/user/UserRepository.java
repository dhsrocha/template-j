package template.feature.user;

import java.util.UUID;
import template.base.contract.Repository;
import template.base.stereotype.Entity;

/**
 * User feature repository implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
final class UserRepository extends Repository.Default<User> {

  @javax.inject.Inject
  UserRepository(final @lombok.NonNull Entity<UUID, User> entity) {
    super(entity);
  }
}
