package template.orm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import template.base.stereotype.Entity;
import template.feature.address.Address;
import template.feature.user.User;

/**
 * General abstraction of a domain object that is going to be persisted.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("unused")
@dagger.Module
public final class EntityManager {

  // TODO Provisional ORM implementations. Should be replaced by a real one.
  private final Map<UUID, User> user = new HashMap<>();
  private final Map<UUID, Address> address = new HashMap<>();

  @javax.inject.Inject
  EntityManager() {
  }

  @dagger.Provides
  static Entity<UUID, User> user(final @NonNull EntityManager em) {
    return () -> em.user;
  }

  @dagger.Provides
  static Entity<UUID, Address> address(final @NonNull EntityManager em) {
    return () -> em.address;
  }
}
