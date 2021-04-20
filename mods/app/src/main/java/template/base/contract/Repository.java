package template.base.contract;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.val;
import org.ehcache.Cache;
import template.base.stereotype.Domain;

/**
 * Ensembles business concerns and database handling. Meant to follow a
 * regular Repository design pattern.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Repository<D extends Domain<D>, I> {

  I create(final @NonNull D record);

  Optional<D> getOne(final @NonNull I id);

  Map<I, D> getMany(final @NonNull D criteria);

  Map<I, D> getAll();

  boolean update(final @NonNull I id, final @NonNull D d);

  boolean delete(final @NonNull I id);

  interface Cached<D extends Domain<D>, I>
      extends Repository<D, I>,
              CacheManager.WithCache<D, I, Repository<D, I>> {
  }

  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  abstract class Default<T extends Domain<T>> implements Repository<T, UUID>,
                                                         Cached<T, UUID> {

    private final Map<UUID, T> store;

    @Override
    public final UUID create(final @NonNull T toCreate) {
      val id = UUID.randomUUID();
      store.put(id, toCreate);
      return id;
    }

    @Override
    public final Optional<T> getOne(final @NonNull UUID id) {
      return Optional.ofNullable(store.get(id));
    }

    @Override
    public final Map<UUID, T> getMany(final @NonNull T t) {
      return store;
    }

    @Override
    public final boolean update(final @NonNull UUID id, final @NonNull T t) {
      return null != store.replace(id, t);
    }

    @Override
    public final Map<UUID, T> getAll() {
      return store;
    }

    @Override
    public final boolean delete(final @NonNull UUID id) {
      return null != store.remove(id);
    }

    @Override
    public final Repository<T, UUID> with(final @NonNull Cache<UUID, T> cache) {
      return new WithCache<>(cache, this);
    }
  }

  @Value
  class WithCache<D extends Domain<D>, I> implements Repository<D, I> {

    Cache<I, D> cache;
    Repository<D, I> repo;

    @Override
    public Optional<D> getOne(final @NonNull I id) {
      return Optional.ofNullable(cache.get(id)).or(() -> repo.getOne(id));
    }

    @Override
    public Map<I, D> getMany(final @NonNull D criteria) {
      return repo.getMany(criteria);
    }

    @Override
    public Map<I, D> getAll() {
      return repo.getAll();
    }

    @Override
    public I create(final @NonNull D user) {
      val id = repo.create(user);
      cache.put(id, user);
      return id;
    }

    @Override
    public boolean update(final @NonNull I id, final @NonNull D user) {
      val updated = repo.update(id, user);
      if (updated) {
        cache.replace(id, user);
      }
      return updated;
    }

    @Override
    public boolean delete(final @NonNull I id) {
      val deleted = repo.delete(id);
      if (deleted) {
        cache.remove(id);
      }
      return deleted;
    }
  }
}
