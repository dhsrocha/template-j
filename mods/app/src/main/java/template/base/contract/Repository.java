package template.base.contract;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.ehcache.Cache;
import template.base.Body;
import template.base.Exceptions;
import template.base.stereotype.Domain;
import template.base.stereotype.Entity;
import template.base.stereotype.Referable;

/**
 * Ensembles business concerns and database handling. Meant to follow a regular
 * <i>Repository</i> design pattern.
 *
 * @param <D> {@link Domain Resource} handled by the implementing operations.
 * @param <I> Represents the {@link D root domain context}'s identity.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Repository<D extends Domain<D>, I> {

  I create(final @NonNull D d);

  Optional<D> getOne(final @NonNull I id);

  Map<I, D> getBy(final @NonNull Body<D> criteria,
                  final int skip, final int limit);

  boolean update(final @NonNull I id, final @NonNull D d);

  boolean delete(final @NonNull I id);

  // ::: Caching :::

  /**
   * An {@link Repository} specialization to allow implementation combine with
   * caching capabilities.
   *
   * @param <D> {@link Domain Resource} handled by the implementing operations.
   * @param <I> Represents the {@link D root domain context}'s identity.
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
   * @param <T> {@link Domain Resource} handled by the implementing operations.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  abstract class Default<T extends Domain<T>> implements Repository<T, UUID>,
                                                         Cached<T, UUID> {

    protected final Dao dao;
    private final Class<T> ref;

    @Override
    public final Optional<T> getOne(final @NonNull UUID id) {
      return dao.from(ref).getOne(id);
    }

    @Override
    public final Map<UUID, T> getBy(final @NonNull Body<T> criteria,
                                    final int skip, final int limit) {
      return dao.from(ref).getBy(criteria, skip, limit);
    }

    @Override
    public final UUID create(final @NonNull T t) {
      return dao.from(ref).create(t);
    }

    @Override
    public final boolean update(final @NonNull UUID id, final @NonNull T t) {
      return dao.from(ref).update(id, t);
    }

    @Override
    public final boolean delete(final @NonNull UUID id) {
      return dao.from(ref).delete(id);
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
   * @param <D> {@link Domain Resource} handled by the implementing operations.
   * @param <I> Represents the {@link D root domain context}'s identity.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  final class CachedDelegate<D extends Domain<D>, I>
      implements Repository<D, I> {

    private final Cache<I, D> cache;
    private final Repository<D, I> repo;

    @Override
    public Optional<D> getOne(final @NonNull I id) {
      return Optional.ofNullable(cache.get(id))
                     .or(() -> repo.getOne(id).map(d -> {
                       cache.put(id, d);
                       return d;
                     }));
    }

    @Override
    public Map<I, D> getBy(final @NonNull Body<D> filter,
                           final int skip, final int limit) {
      val store = repo.getBy(filter, skip, limit);
      cache.putAll(store);
      return store;
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

  // ::: Composition :::

  /**
   * Repository specialization which handles resources from two different
   * {@link Domain domain contexts}.
   *
   * @param <T> {@link Domain resource} which the association will be based on.
   * @param <U> {@link Domain resource} handled by the implemented operations.
   * @param <I> Represents the {@link T root domain context}'s identity.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see Composed
   * @see CompositeDelegate
   */
  interface Composable<T extends Domain<T>, U extends Domain<U>, I> {

    /**
     * Composes handling operations based on resources related to an another
     * {@link Domain domain context}, which the {@link I provided identity} is
     * related to.
     *
     * @param id        Identity from root {@link T domain context's resource}.
     *                  To be used on bound structures' join operations.
     * @param composite {@link Service} instance which handles resources from
     *                  projected domain context.
     * @param isValid   Enforces business rule constraints between resources
     *                  from two distinct domain contexts.
     * @return The instance provided for the composition.
     * @see CompositeDelegate
     */
    Repository<U, I> compose(final @NonNull I id,
                             final @NonNull Service<U, I> composite,
                             final @NonNull Function<T, Predicate<U>> isValid);
  }

  /**
   * Default {@link Repository} abstraction with {@link Service} composing
   * capabilities. Meant to openly extendable.
   *
   * @param <T> {@link Domain resource} which the association will be based on.
   * @param <U> {@link Domain resource} handled by the following operations.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see Composable
   * @see CompositeDelegate
   */
  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  abstract class Composed<T extends Domain<T>, U extends Domain<U>>
      implements Composable<T, U, UUID>,
                 Referable<U> {

    private final Repository<T, UUID> repo;
    private final Entity.WithJoin<UUID, T, U> join;

    @Override
    public Repository<U, UUID> compose(
        final @NonNull UUID root,
        final @NonNull Service<U, UUID> svc,
        final @NonNull Function<T, Predicate<U>> bind) {
      val set = join.from(ref(), root);
      return repo.getOne(root).map(bind)
                 .map(p -> new CompositeDelegate<>(set, p, svc))
                 .orElseThrow(Exceptions.RESOURCE_NOT_FOUND);
    }
  }

  /**
   * Delegate implementation based on resources related to an another
   * {@link Domain domain context}, which the {@link I provided identity} is
   * related to.
   *
   * @param <U> {@link Domain resource} handled by the implemented operations.
   * @param <I> Represents the {@link U domain context}'s identity.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see Composable
   * @see Composed
   */
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  final class CompositeDelegate<U extends Domain<U>, I>
      implements Repository<U, I> {

    private final Set<I> joined;
    private final Predicate<U> isValid;
    private final Service<U, I> extent;

    @Override
    public I create(final @NonNull U toCreate) {
      Exceptions.UNPROCESSABLE_ENTITY.throwIf(() -> !isValid.test(toCreate));
      val i = extent.create(toCreate);
      Exceptions.CANNOT_BIND_UNBIND.throwIf(() -> !joined.add(i));
      return i;
    }

    @Override
    public Optional<U> getOne(final @NonNull I id) {
      return Optional.of(id).filter(joined::contains).map(extent::getOne);
    }

    @Override
    public Map<I, U> getBy(final @NonNull Body<U> criteria,
                           final int skip, final int limit) {
      return extent.getBy(criteria, skip, limit).entrySet().stream()
                   .filter(e -> joined.contains(e.getKey()))
                   .collect(Collectors.toMap(Map.Entry::getKey,
                                             Map.Entry::getValue));
    }

    @Override
    public boolean update(final @NonNull I id, final @NonNull U toUpdate) {
      return isValid.test(extent.getOne(id)) && joined.add(id);
    }

    @Override
    public boolean delete(final @NonNull I id) {
      return isValid.test(extent.getOne(id)) && joined.remove(id);
    }
  }
}
