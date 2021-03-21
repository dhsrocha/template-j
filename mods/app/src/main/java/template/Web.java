package template;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.annotations.ContentType;
import java.util.function.Supplier;
import template.Application.Mode;
import template.Web.Mod;
import template.base.contract.Builder;

/**
 * Module for bootstrapping application's web server.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">>Diego Rocha</a>
 */
@Application.Scope
@dagger.Component(dependencies = Router.class, modules = Mod.class)
interface Web extends Supplier<Javalin> {

  @dagger.Module
  interface Mod {
    @dagger.Provides
    @Application.Scope
    static Javalin server(final @lombok.NonNull Mode mode,
                          final @lombok.NonNull Router routes) {
      return Javalin.create(cfg -> {
        cfg.showJavalinBanner = mode == Mode.PRD;
        cfg.defaultContentType = ContentType.JSON;
        cfg.autogenerateEtags = Boolean.TRUE;
      }).routes(routes.get());
    }
  }

  @dagger.Component.Builder
  interface Build extends Builder.Part1<Build, Web, Mode>,
                          Builder.Dependency1<Build, Web, Router> {
  }
}
