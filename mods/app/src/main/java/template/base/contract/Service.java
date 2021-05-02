package template.base.contract;

import java.util.Map;
import java.util.function.Predicate;
import lombok.AccessLevel;
import template.base.Exceptions;
import template.base.stereotype.Domain;

/**
 * Describes general api for handling MVC operations. Meant to be used along
 * with {@link Controller} on ReST templates.
 *
 * @param <T> To be handled among the operations.
 * @param <I> To be used as an the domain indexer.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Service<T, I> {

  T getOne(final @lombok.NonNull I id);

  Map<I, T> getBy(final @lombok.NonNull Predicate<T> criteria,
                  final int skip, final int limit);

  I create(final @lombok.NonNull T t);

  boolean update(final @lombok.NonNull I id, final @lombok.NonNull T t);

  boolean delete(final @lombok.NonNull I id);

  default Predicate<T> filter(@lombok.NonNull final T t) {
    return Predicate.isEqual(t);
  }

  /**
   * Default abstraction, meant to be openly extendable. Natively supports
   * caching.
   *
   * @param <D> {@link Domain} type to be handled among the operations.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @lombok.AllArgsConstructor(access = AccessLevel.PROTECTED)
  abstract class Cached<D extends Domain<D>, I> implements Service<D, I>,
                                                           Domain.Ref<D> {

    private final CacheManager<D, I> cache;
    private final Repository.Cached<D, I> repo;

    @Override
    public D getOne(final @lombok.NonNull I id) {
      return repo.with(cache.from(domainRef())).getOne(id)
                 .orElseThrow(Exceptions.RESOURCE_NOT_FOUND);
    }

    @Override
    public Map<I, D> getBy(final @lombok.NonNull Predicate<D> criteria,
                           final int skip, final int limit) {
      return repo.with(cache.from(domainRef())).getBy(criteria, skip, limit);
    }

    @Override
    public I create(final @lombok.NonNull D user) {
      return repo.with(cache.from(domainRef())).create(user);
    }

    @Override
    public boolean update(final @lombok.NonNull I id,
                          final @lombok.NonNull D user) {
      return repo.with(cache.from(domainRef())).update(id, user);
    }

    @Override
    public boolean delete(final @lombok.NonNull I id) {
      return repo.with(cache.from(domainRef())).delete(id);
    }
  }
}
