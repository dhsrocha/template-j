package template.base.contract;

import java.util.Map;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import template.base.Exceptions;
import template.base.stereotype.Domain;
import template.base.stereotype.Referable;

/**
 * Describes general api for handling MVC operations. Meant to be used along
 * with {@link Controller} on ReST templates.
 *
 * @param <T> Resource handled by the implementing operations.
 * @param <I> Represents the {@link T domain context}'s identity.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Service<T, I> {

  T getOne(final @NonNull I id);

  Map<I, T> getBy(final @NonNull Predicate<T> criteria,
                  final int skip, final int limit);

  I create(final @NonNull T t);

  boolean update(final @NonNull I id, final @NonNull T t);

  boolean delete(final @NonNull I id);

  default Predicate<T> filter(@NonNull final T t) {
    return Predicate.isEqual(t);
  }

  // ::: Cached :::

  /**
   * Abstraction which natively supports caching capabilities. Meant to be
   * openly extendable.
   *
   * @param <D> {@link Domain Resource} handled by the implementing operations.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  abstract class Cached<D extends Domain<D>, I> implements Service<D, I>,
                                                           Referable<D> {

    private final CacheManager<D, I> cache;
    private final Repository.Cached<D, I> repo;

    @Override
    public D getOne(final @NonNull I id) {
      return repo.with(cache.from(ref())).getOne(id)
                 .orElseThrow(Exceptions.RESOURCE_NOT_FOUND);
    }

    @Override
    public Map<I, D> getBy(final @NonNull Predicate<D> criteria,
                           final int skip, final int limit) {
      return repo.with(cache.from(ref())).getBy(criteria, skip, limit);
    }

    @Override
    public I create(final @NonNull D user) {
      return repo.with(cache.from(ref())).create(user);
    }

    @Override
    public boolean update(final @NonNull I id,
                          final @NonNull D user) {
      return repo.with(cache.from(ref())).update(id, user);
    }

    @Override
    public boolean delete(final @NonNull I id) {
      return repo.with(cache.from(ref())).delete(id);
    }
  }
}
