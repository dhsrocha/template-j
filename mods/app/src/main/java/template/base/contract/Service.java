package template.base.contract;

import java.util.Map;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import template.base.Body;
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

  Map<I, T> getBy(final @NonNull Body<T> criteria,
                  final int skip, final int limit);

  I create(final @NonNull T t);

  boolean update(final @NonNull I id, final @NonNull T t);

  boolean delete(final @NonNull I id);

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
    public Map<I, D> getBy(final @NonNull Body<D> criteria,
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

  // ::: Composition :::

  /**
   * Handles CRUD operations based on the a provided domain resource.
   *
   * @param <D> Resource from which operations are based on.
   * @param <E> Resource handled by the extension operations.
   * @param <I> Represents the {@link D domain context}'s identity.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see Composed
   */
  interface Composable<D, E, I> {

    Map<I, E> getByFrom(final @NonNull I root,
                        final @NonNull Body<E> criteria,
                        final int skip, final int limit);

    E getOneFrom(final @NonNull I root, final @NonNull I id);

    I createOn(final @NonNull I root, final @NonNull E e);

    boolean link(final @NonNull I root, final @NonNull I id);

    boolean unlink(final @NonNull I root, final @NonNull I id);

    default Predicate<E> isValidBound(final @NonNull D d) {
      return e -> Boolean.TRUE;
    }
  }

  /**
   * General abstraction which composes abstractions from distinct domain
   * contexts. Meant to be openly extendable.
   *
   * @param <D> {@link Domain Resource} from which operations are based on.
   * @param <E> {@link Domain Resource} handled by the extension operations.
   * @param <I> Represents the {@link D domain context}'s identity.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see Composable
   */
  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  abstract class Composed<D extends Domain<D>, E extends Domain<E>, I>
      implements Composable<D, E, I> {

    private final Repository.Composable<D, E, I> base;

    @Override
    public Map<I, E> getByFrom(final @NonNull I root,
                               final @NonNull Body<E> criteria,
                               final int s, final int l) {
      return base.compose(root, this::isValidBound).getBy(criteria, s, l);
    }

    @Override
    public E getOneFrom(final @NonNull I root, final @NonNull I id) {
      return base.compose(root, this::isValidBound).getOne(id)
                 .orElseThrow(Exceptions.RESOURCE_NOT_FOUND);
    }

    @Override
    public I createOn(final @NonNull I root, final @NonNull E e) {
      return base.compose(root, this::isValidBound).create(e);
    }

    @Override
    public boolean link(final @NonNull I root, final @NonNull I id) {
      return base.compose(root, this::isValidBound).link(id);
    }

    @Override
    public boolean unlink(final @NonNull I root, final @NonNull I id) {
      return base.compose(root, this::isValidBound).unlink(id);
    }
  }
}
