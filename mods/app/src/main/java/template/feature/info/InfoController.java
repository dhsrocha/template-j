package template.feature.info;

import java.util.Arrays;
import java.util.Map;
import template.Application.Feat;
import template.Props;
import template.base.contract.Controller;

/**
 * Controller for application information feature.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
final class InfoController implements Controller.Getter<Info> {

  private final Feat[] feats;

  @javax.inject.Inject
  InfoController(final Feat[] feats) {
    this.feats = feats;
  }

  @Override
  public Info get() {
    return Info.of(Map.of(Props.FEAT.name(), Arrays.toString(feats)));
  }
}