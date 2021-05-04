package template.orm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import template.base.stereotype.Entity;
import template.base.stereotype.Entity.WithJoin;
import template.feature.address.Address;
import template.feature.auth.Auth;
import template.feature.user.User;

/**
 * General abstraction of a domain object that is going to be persisted.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
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
  private final Map<UUID, Auth> auth = new HashMap<>();
  // Joins
  private final Map<Class<?>, Map<UUID, Set<UUID>>> userJoins = Stream
      .of(Address.class)
      .map(c -> Map.entry(c, new HashMap<UUID, Set<UUID>>()))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

  @javax.inject.Inject
  DataAccess() {
  }

  @DataAccess.Scope
  @dagger.Provides
  static Entity<UUID, User> user(final @NonNull DataAccess da) {
    return () -> da.user;
  }

  @DataAccess.Scope
  @dagger.Provides
  static Entity<UUID, Address> address(final @NonNull DataAccess da) {
    return () -> da.address;
  }

  @DataAccess.Scope
  @dagger.Provides
  static Entity.WithJoin<UUID, User, Address> uaJoin(
      final @NonNull DataAccess da) {
    return new WithJoin<>() {
      @Override
      public Map<UUID, User> getStore() {
        return da.user;
      }

      @Override
      public Set<UUID> from(final @NonNull Class<Address> ref,
                            final @NonNull UUID id) {
        da.userJoins.get(ref).putIfAbsent(id, new HashSet<>());
        return da.userJoins.get(ref).get(id);
      }
    };
  }

  @dagger.Provides
  static Entity<UUID, Auth> auth(final @NonNull DataAccess em) {
    return () -> em.auth;
  }
}
