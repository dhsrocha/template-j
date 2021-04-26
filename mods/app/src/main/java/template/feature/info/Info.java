package template.feature.info;

import java.util.Map;
import lombok.Value;
import template.base.contract.Controller;
import template.base.contract.Routes;

/**
 * Application information object. It is designed to hold general information of
 * for health check purposes.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Value(staticConstructor = "of")
public class Info {

  Map<String, String> map;

  /**
   * Type for binding package-private implementations to public interfaces.
   * It is meant to be included into a {@link Routes} managed module.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @SuppressWarnings("unused")
  @dagger.Module
  public interface Mod {

    @dagger.Binds
    Controller.Getter<Info> controller(final InfoController u);
  }
}
