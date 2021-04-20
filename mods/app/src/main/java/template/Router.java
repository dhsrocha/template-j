package template;

import io.javalin.apibuilder.ApiBuilder;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.function.Supplier;
import template.Application.Feat;
import template.Application.Mode;
import template.Router.FeatureScope;
import template.Router.Mod;
import template.base.contract.Builder;
import template.base.contract.Controller;
import template.base.contract.Routes;
import template.feature.info.Info;
import template.feature.user.User;

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

  @dagger.Module(includes = {Info.Mod.class, User.Mod.class})
  interface Mod {

    @FeatureScope
    @dagger.Provides
    static Routes routes(final @lombok.NonNull Application.Mode mode,
                         final @lombok.NonNull Application.Feat[] feats,
                         final @lombok.NonNull Controller.Getter<Info> info,
                         final @lombok.NonNull Controller<User> user) {
      return () -> {
        if (Mode.PRD != mode) {
          ApiBuilder.get(info);
        }
        for (final var f : feats) {
          if (Feat.USER == f) {
            ApiBuilder.crud(user.crudPath(), user);
          }
        }
      };
    }
  }

  @dagger.Component.Builder
  interface Build extends Builder.Part1<Build, Router, Application.Mode>,
                          Builder.Part2<Build, Router, Application.Feat[]> {
  }
}
