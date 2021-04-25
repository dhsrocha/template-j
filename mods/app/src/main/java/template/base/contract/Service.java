package template.base.contract;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Describes general api for handling MVC operations. Meant to be used along
 * with {@link Controller} and ReST templates.
 *
 * @param <T> To be handled among the operations.
 * @param <I> To be used as an the domain indexer.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Service<T, I> {

  T getOne(final @lombok.NonNull I id);

  Map<I, T> getBy(final @lombok.NonNull Predicate<T> criteria);

  Map<I, T> getAll();

  I create(final @lombok.NonNull T t);

  boolean update(final @lombok.NonNull I id, final @lombok.NonNull T t);

  boolean delete(final @lombok.NonNull I id);

  Predicate<T> filter(final @lombok.NonNull T criteria);
}
