package template.feature.info;

import java.util.Map;
import lombok.Value;
import template.base.contract.Controller;
import template.base.contract.Router;

/**
 * Application information object. It is designed to hold general information of
 * for health check purposes.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Value(staticConstructor = "of")
public class Info {

  Map<String, String> feats;

  /**
   * Type for binding package-private implementations to public interfaces.
   * It is meant to be included into a {@link Router} managed module.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @SuppressWarnings("unused")
  @dagger.Module
  public interface Mod {

    @dagger.Binds
    Controller.Single<Info> controller(final InfoController u);
  }
}
