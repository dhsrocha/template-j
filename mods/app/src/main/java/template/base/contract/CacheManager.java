package template.base.contract;

import dagger.Module;
import java.time.Duration;
import java.util.Optional;
import lombok.NonNull;
import org.ehcache.Cache;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import template.base.stereotype.Domain;
import template.base.stereotype.Referable;

/**
 * Manager cache concerns for a given {@link Domain domain} scope.
 *
 * @param <D> {@link Domain} type to be handled among the operations.
 * @param <I> A type to be used as an indexer.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface CacheManager<D extends Domain<D>, I> {

  /**
   * {@link Cache} factory method to retrieve for a specific {@link Domain}
   * type or, alternatively, to create a new one if needed and then retrieve it.
   *
   * @param ref A {@link Domain domain} class reference.
   * @return A retrieved or recently created cache meant to index instance of a
   *     specific {@link Domain} type.
   */
  Cache<I, D> from(final @NonNull Class<D> ref);

  /**
   * Contract to allow other abstractions to be composed with caching
   * capabilities.
   *
   * @param <D> {@link Domain} type to be handled among the operations.
   * @param <I> A type to be used as an indexer.
   * @param <R> Meant to return the abstraction which is being combined to.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  interface WithCache<D extends Domain<D>, I, R> {

    /**
     * Allows other implementations' concerns combines themselves with the
     * caching ones.
     *
     * @param cache Cache manager prepared for a given domain context.
     * @return The abstraction which is being combined to.
     */
    R with(final @NonNull CacheManager<D, I> cache);
  }

  /**
   * Default abstraction for a {@link Repository}. Meant to openly extendable.
   *
   * @param <D> {@link Domain} type to be handled among the operations.
   * @param <I> A type to be used as an the domain indexer.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @Module
  abstract class Default<D extends Domain<D>, I> implements CacheManager<D, I>,
                                                            Referable<D> {

    private static final ResourcePoolsBuilder POOL = ResourcePoolsBuilder
        .heap(10);
    private final org.ehcache.CacheManager manager;

    protected Default() {
      this.manager = CacheManagerBuilder.newCacheManagerBuilder().build();
      manager.init();
    }

    @Override
    public final Cache<I, D> from(final @NonNull Class<D> ref) {
      return Optional.ofNullable(manager.getCache(
          ref.getSimpleName(), idRef(), ref())).orElseGet(() -> manager
          .createCache(ref.getSimpleName(), defaultConfig()));
    }

    protected CacheConfigurationBuilder<I, D> defaultConfig() {
      return CacheConfigurationBuilder
          .newCacheConfigurationBuilder(idRef(), ref(), POOL)
          .withExpiry(ExpiryPolicyBuilder.expiry()
                                         .access(Duration.ofMinutes(5))
                                         .build());
    }

    protected abstract Class<I> idRef();
  }
}
