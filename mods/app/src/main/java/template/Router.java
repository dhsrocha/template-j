package template;

import io.javalin.apibuilder.ApiBuilder;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.function.Supplier;
import template.Application.Feat;
import template.Router.FeatureScope;
import template.Router.Mod;
import template.base.contract.Builder;
import template.base.contract.Routes;

/**
 * Component for exposing application's ReST resources. Ultimately, assembles
 * and decides which features should be activated and resolved.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@FeatureScope
@dagger.Component(modules = Mod.class)
interface Router extends Supplier<Routes> {

  @javax.inject.Scope
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface FeatureScope {
  }

  @dagger.Module
  interface Mod {

    @FeatureScope
    @dagger.Provides
    static Routes routes(final @lombok.NonNull Feat[] feats) {
      return () -> ApiBuilder.get(ctx -> ctx.result(Arrays.toString(feats)));
    }
  }

  @dagger.Component.Builder
  interface Build extends Builder.Part1<Build, Router, Feat[]> {
  }
}
