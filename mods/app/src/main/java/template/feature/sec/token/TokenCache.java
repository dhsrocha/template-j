package template.feature.sec.token;

import java.time.Duration;
import java.util.UUID;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import template.base.contract.CacheManager;

/**
 * {@link Token} feature cache manager implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
final class TokenCache extends CacheManager.Default<Token, UUID> {

  @Override
  public Class<Token> ref() {
    return Token.class;
  }

  @Override
  protected Class<UUID> idRef() {
    return UUID.class;
  }

  @Override
  protected CacheConfigurationBuilder<UUID, Token> defaultConfig() {
    return super.defaultConfig()
                .withResourcePools(() -> ResourcePoolsBuilder
                    .heap(100000).build())
                .withExpiry(ExpiryPolicyBuilder
                                .expiry().access(Duration.ofMinutes(1))
                                .build());
  }
}
