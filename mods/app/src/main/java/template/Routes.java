package template;

import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import java.util.Arrays;
import javax.inject.Inject;
import template.Application.Feat;

final class Routes implements EndpointGroup {

  private final Feat[] feats;

  @Inject
  Routes(final Feat[] feats) {
    this.feats = feats;
  }

  @Override
  public void addEndpoints() {
    ApiBuilder.get(ctx -> ctx.result("It works! Activated features:"
                                         + Arrays.toString(feats)));
  }
}
