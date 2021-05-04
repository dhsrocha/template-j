package template.feature.sec.token;

import java.util.UUID;
import lombok.NonNull;
import lombok.val;
import org.ehcache.Cache;
import template.base.contract.Controller;

/**
 * {@link Token} feature controller implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
final class TokenController implements Controller.Single<Token> {

  private final Cache<UUID, Token> cache;

  @javax.inject.Inject
  TokenController(final @NonNull TokenCache cache) {
    this.cache = cache.from(Token.class);
  }

  @Override
  public Class<Token> ref() {
    return Token.class;
  }

  @Override
  public Token get() {
    val id = UUID.randomUUID();
    val t = Token.generate(id);
    cache.put(id, t);
    return t;
  }
}
