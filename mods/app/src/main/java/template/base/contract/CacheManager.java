package template.base.contract;

import dagger.Module;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import org.ehcache.Cache;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import template.base.stereotype.Domain;

/**
 * Manager cache concerns for a given {@link Domain domain} scope.
 *
 * @param <D> the {@link Domain} type.
 * @param <I> the type to identify objects.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface CacheManager<D extends Domain<D>, I> {

  Cache<I, D> from(final @NonNull Class<D> ref);

  interface WithCache<D extends Domain<D>, I, R> {

    R with(final @NonNull Cache<I, D> cache);
  }

  @Module
  abstract class Default<D extends Domain<D>, I> implements CacheManager<D, I>,
                                                            Domain.Ref<D> {

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
          ref.getSimpleName(), idRef(), domainRef())).orElseGet(() -> manager
          .createCache(ref.getSimpleName(), defaultConfig()));
    }

    protected CacheConfigurationBuilder<I, D> defaultConfig() {
      return CacheConfigurationBuilder
          .newCacheConfigurationBuilder(idRef(), domainRef(), POOL)
          .withExpiry(Expirations.timeToLiveExpiration(
              Duration.of(5, TimeUnit.MINUTES)));
    }

    protected abstract Class<I> idRef();
  }
}
