package template.base.contract;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterators;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
 * @param <D> {@link Domain} type to be handled among the operations.
 * @param <I> A type to be used as an the domain indexer.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Repository<D extends Domain<D>, I> {

  I create(final @NonNull D d);

  Optional<D> getOne(final @NonNull I id);

  Map<I, D> getBy(final @NonNull Predicate<D> criteria);

  Map<I, D> getAll();

  boolean update(final @NonNull I id, final @NonNull D d);

  boolean delete(final @NonNull I id);

  /**
   * An {@link Repository} specialization to allow implementation combine
   * with caching capabilities.
   *
   * @param <D> {@link Domain} type to be handled among the operations.
   * @param <I> A type to be used as an the domain indexer.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see CacheManager
   * @see CachedDelegate
   */
  interface Cached<D extends Domain<D>, I>
      extends Repository<D, I>,
              CacheManager.WithCache<D, I, Repository<D, I>> {
  }

  /**
   * Default {@link Repository} abstraction. Meant to openly extendable.
   *
   * @param <T> {@link Domain} type to be handled among the operations.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  abstract class Default<T extends Domain<T>> implements Repository<T, UUID>,
                                                         Cached<T, UUID> {

    private final Map<UUID, T> store;

    @Override
    public final Optional<T> getOne(final @NonNull UUID id) {
      return Optional.ofNullable(store.get(id));
    }

    @Override
    public final Map<UUID, T> getBy(final @NonNull Predicate<T> filter) {
      return store
          .entrySet().stream().filter(e -> filter.test(e.getValue()))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public final Map<UUID, T> getAll() {
      return store;
    }

    @Override
    public final UUID create(final @NonNull T d) {
      val id = UUID.randomUUID();
      store.put(id, d);
      return id;
    }

    @Override
    public final boolean update(final @NonNull UUID id, final @NonNull T t) {
      return null != store.replace(id, t);
    }

    @Override
    public final boolean delete(final @NonNull UUID id) {
      return null != store.remove(id);
    }

    @Override
    public final Repository<T, UUID> with(final @NonNull Cache<UUID, T> cache) {
      return new CachedDelegate<>(cache, this);
    }
  }

  /**
   * Delegate implementation which combines {@link Repository storing} and
   * {@link Cache caching} capabilities.
   *
   * @param <D> {@link Domain} type to be handled among the operations.
   * @param <I> A type to be used as an the domain indexer.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @Value
  class CachedDelegate<D extends Domain<D>, I> implements Repository<D, I> {

    Cache<I, D> cache;
    Repository<D, I> repo;

    @Override
    public Optional<D> getOne(final @NonNull I id) {
      return Optional.ofNullable(cache.get(id)).or(() -> repo.getOne(id));
    }

    @Override
    public Map<I, D> getBy(final @NonNull Predicate<D> filter) {
      return StreamSupport
          .stream(Spliterators.spliteratorUnknownSize(cache.iterator(), 0),
                  Boolean.FALSE)
          .filter(e -> filter.test(e.getValue()))
          .map(e -> new SimpleEntry<>(e.getKey(), e.getValue()))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<I, D> getAll() {
      val all = repo.getAll();
      cache.putAll(all);
      return cache.getAll(all.keySet());
    }

    @Override
    public I create(final @NonNull D d) {
      val id = repo.create(d);
      cache.put(id, d);
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
