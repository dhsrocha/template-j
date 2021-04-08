package template;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.annotations.ContentType;
import java.util.function.Supplier;
import lombok.val;
import template.Application.Mode;
import template.Web.Mod;
import template.Web.Server;
import template.base.contract.Builder;
import template.base.contract.Builder.Dep1;

/**
 * Module for bootstrapping application's web server.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Application.Scope
@dagger.Component(dependencies = Router.Build.class, modules = Mod.class)
interface Web extends Supplier<Server> {

  interface Server {

    Server start(final int port);

    void stop();
  }

  @dagger.Module
  interface Mod {
    @dagger.Provides
    @Application.Scope
    static Server server(final @lombok.NonNull Mode mode,
                         final @lombok.NonNull Router.Build routes) {
      val app = Javalin.create(cfg -> {
        cfg.showJavalinBanner = mode == Mode.PRD;
        cfg.defaultContentType = ContentType.JSON;
        cfg.autogenerateEtags = Boolean.TRUE;
      }).routes(routes.build().get());
      Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
      return new Server() {

        @Override
        public Server start(final int port) {
          app.start(port);
          return this;
        }

        @Override
        public void stop() {
          app.stop();
        }
      };
    }
  }

  @dagger.Component.Builder
  interface Build extends Builder.Part1<Build, Web, Mode>,
                          Dep1<Build, Web, Router.Build> {
  }
}
