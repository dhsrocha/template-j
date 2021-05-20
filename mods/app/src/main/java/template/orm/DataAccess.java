package template.orm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
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
public final class DataAccess {

  /**
   * Meant to scope elements the {@link DataAccess} module.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @Documented
  @javax.inject.Scope
  @Target({ElementType.TYPE, ElementType.METHOD})
  public @interface Scope {
  }

  // TODO Provisional ORM implementations. Should be replaced by a real one.
  private final Map<UUID, User> user = new HashMap<>();
  private final Map<UUID, Address> address = new HashMap<>();

  @javax.inject.Inject
  DataAccess() {
  }

  @dagger.Provides
  static Entity<UUID, User> user(final @NonNull DataAccess em) {
    return () -> em.user;
  }

  @dagger.Provides
  static Entity<UUID, Address> address(final @NonNull DataAccess em) {
    return () -> em.address;
  }
}
