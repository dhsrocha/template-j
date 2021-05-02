package template.base.stereotype;

import java.util.Map;
import java.util.Set;
import lombok.NonNull;

/**
 * Abstraction of a domain object which data is handled at persistence layer.
 *
 * @param <D> {@link Domain} type which the association will be based on.
 * @param <I> A type to be used as an the domain indexer.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Entity<I, D> {

  Map<I, D> getStore();

  /**
   * An {@link Entity} specialization which brings along the associated
   * entity's references from another {@link E domain} context.
   *
   * @param <D> {@link Domain} type which the association will be based on.
   * @param <E> {@link Domain} type to be handled by the following operations.
   * @param <I> Represents the {@link D root domain context}'s identity.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  interface WithJoin<I, D, E> extends Entity<I, D> {

    /**
     * Retrieves the persistence associative representation relates to a given
     * domain key.
     *
     * @param ref Domain reference.
     * @param id  Root identity key.
     * @return The bound keys indexed by the provided key.
     */
    Set<I> from(final @NonNull Class<E> ref, final @NonNull I id);
  }
}
