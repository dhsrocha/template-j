package template;

import dagger.Module;
import dagger.Provides;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import java.util.Arrays;
import lombok.NonNull;
import template.Application.Feat;

@Module
interface Router extends EndpointGroup {

  @Provides
  static Router routes(final @NonNull Feat[] feats) {
    return () -> ApiBuilder.get(ctx -> ctx.result(Arrays.toString(feats)));
  }
}
