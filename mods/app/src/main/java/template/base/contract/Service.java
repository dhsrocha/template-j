package template.base.contract;

import java.util.Map;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import template.base.Body;
import template.base.Exceptions;
import template.base.stereotype.Domain;

/**
 * Describes general api for handling MVC operations. Meant to be used along
 * with {@link Controller} on ReST templates.
 *
 * @param <T> Resource handled by the implementing operations.
 * @param <I> Represents the {@link T domain context}'s identity.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Service<T, I> {

  T get(final @NonNull I id);

  Map<I, T> get(final @NonNull Body<T> criteria,
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
  abstract class Cached<D extends Domain<D>, I> implements Service<D, I> {

    private final CacheManager<D, I> cache;
    private final Repository.Cached<D, I> repo;

    @Override
    public D get(final @NonNull I id) {
      return repo.with(cache).get(id).orElseThrow(Exceptions.NOT_FOUND);
    }

    @Override
    public Map<I, D> get(final @NonNull Body<D> criteria,
                         final int skip, final int limit) {
      return repo.with(cache).get(criteria, skip, limit);
    }

    @Override
    public I create(final @NonNull D user) {
      return repo.with(cache).create(user);
    }

    @Override
    public boolean update(final @NonNull I id,
                          final @NonNull D user) {
      return repo.with(cache).update(id, user);
    }

    @Override
    public boolean delete(final @NonNull I id) {
      return repo.with(cache).delete(id);
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

    Map<I, E> getFrom(final @NonNull I root,
                      final @NonNull Body<E> criteria,
                      final int skip, final int limit);

    E getFrom(final @NonNull I root, final @NonNull I id);

    I createOn(final @NonNull I root, final @NonNull E e);

    boolean link(final @NonNull I root, final @NonNull I id);

    boolean unlink(final @NonNull I root, final @NonNull I id);

    /**
     * Verifies availability for binding resources from two distinct
     * {@link Domain domain contexts}, by evaluating potential state
     * inconsistencies from both.
     *
     * @param toBind To input the resource handled by base operations.
     * @return Predicate to apply on extension operations' handled resource.
     */
    default Predicate<E> isValidToBind(final @NonNull D toBind) {
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
    public Map<I, E> getFrom(final @NonNull I root,
                             final @NonNull Body<E> criteria,
                             final int s, final int l) {
      return base.compose(root, this::isValidToBind).get(criteria, s, l);
    }

    @Override
    public E getFrom(final @NonNull I root, final @NonNull I id) {
      return base.compose(root, this::isValidToBind).get(id)
                 .orElseThrow(Exceptions.NOT_FOUND);
    }

    @Override
    public I createOn(final @NonNull I root, final @NonNull E e) {
      return base.compose(root, this::isValidToBind).create(e);
    }

    @Override
    public boolean link(final @NonNull I root, final @NonNull I id) {
      return base.compose(root, this::isValidToBind).link(id);
    }

    @Override
    public boolean unlink(final @NonNull I root, final @NonNull I id) {
      return base.compose(root, this::isValidToBind).unlink(id);
    }
  }
}
