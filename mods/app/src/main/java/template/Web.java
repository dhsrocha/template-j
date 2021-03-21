package template;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.javalin.Javalin;
import io.javalin.plugin.openapi.annotations.ContentType;
import java.util.function.Supplier;
import lombok.NonNull;
import template.Application.Mode;
import template.Web.Mod;
import template.base.contract.Builder;
import template.base.contract.Routes;

/**
 * Module for bootstrapping application's web server.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">>Diego Rocha</a>
 */
@Application.Scope
@Component(modules = Mod.class)
interface Web extends Supplier<Javalin> {

  @Module
  interface Mod {
    @Provides
    @Application.Scope
    static Javalin server(final @NonNull Mode mode,
                          final @NonNull Routes routes) {
      return Javalin.create(cfg -> {
        cfg.showJavalinBanner = mode != Mode.DEV;
        cfg.defaultContentType = ContentType.JSON;
        cfg.autogenerateEtags = Boolean.TRUE;
      }).routes(routes);
    }
  }

  @dagger.Component.Builder
  interface Build extends Builder.Part1<Build, Web, Mode>,
                          Builder.Part2<Build, Web, Routes> {
  }
}
