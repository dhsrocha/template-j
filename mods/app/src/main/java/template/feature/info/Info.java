package template.feature.info;

import java.util.Map;
import lombok.Value;
import template.base.contract.Controller;

/**
 * Application information object. It is designed to hold general information of
 * for health check purposes.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Value(staticConstructor = "of")
public class Info {

  Map<String, String> map;

  @dagger.Module
  public interface Mod {

    @dagger.Binds
    Controller.Getter<Info> controller(final InfoController u);
  }
}
