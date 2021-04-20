package template.feature.user;

import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import lombok.NonNull;
import template.base.Exceptions;
import template.base.contract.CacheManager;
import template.base.contract.Controller;
import template.base.contract.Repository;

/**
 * User feature controller implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
final class UserController implements Controller<User> {

  private final CacheManager<User, UUID> cache;
  private final Repository.Cached<User, UUID> repo;

  @Inject
  UserController(final @NonNull CacheManager<User, UUID> cache,
                 final @NonNull Repository.Cached<User, UUID> repo) {
    this.cache = cache;
    this.repo = repo;
  }

  @Override
  public Class<User> domainRef() {
    return User.class;
  }

  @Override
  public User getOne(final @NonNull UUID id) {
    return repo.with(cache.from(User.class)).getOne(id)
               .orElseThrow(Exceptions.RESOURCE_NOT_FOUND::create);
  }

  @Override
  public Map<UUID, User> getBy(final @NonNull User criteria) {
    return repo.getMany(criteria);
  }

  @Override
  public Map<UUID, User> getAll() {
    return repo.getAll();
  }

  @Override
  public UUID create(final @NonNull User user) {
    return repo.with(cache.from(User.class)).create(user);
  }

  @Override
  public boolean update(final @NonNull UUID id, final @NonNull User user) {
    return repo.with(cache.from(User.class)).update(id, user);
  }

  @Override
  public boolean delete(final @NonNull UUID id) {
    return repo.with(cache.from(User.class)).delete(id);
  }
}
