package template.core;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJson;
import io.javalin.plugin.openapi.annotations.ContentType;
import java.util.Map;
import java.util.function.Supplier;
import lombok.val;
import template.Application;
import template.Application.Mode;
import template.base.contract.Builder;
import template.base.stereotype.Domain.Violation;
import template.core.Web.Mod;

/**
 * Module for bootstrapping application's web server.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Application.Scope
@dagger.Component(dependencies = Router.Build.class, modules = Mod.class)
interface Web extends Supplier<Application.Server> {


  /**
   * Type for creating instances managed by Dagger.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @dagger.Module
  interface Mod {
    @dagger.Provides
    @Application.Scope
    static Application.Server server(final @lombok.NonNull Mode mode,
                                     final @lombok.NonNull Router.Build routes) {
      val mapper = new Gson();
      JavalinJson.setFromJsonMapper(mapper::fromJson);
      JavalinJson.setToJsonMapper(mapper::toJson);
      val app = Javalin.create(cfg -> {
        cfg.showJavalinBanner = mode == Mode.PRD;
        cfg.defaultContentType = ContentType.JSON;
        cfg.autogenerateEtags = Boolean.TRUE;
      }).routes(routes.build().get());
      Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
      app.exception(Violation.class, (e, c) -> {
        c.status(422);
        c.result(mapper.toJson(Map.of(
            "status", c.status(),
            "message", "Violation rule broken.",
            "violations", e.getInvariants()
        )));
      });
      return new Application.Server() {

        @Override
        public Application.Server start(final int port) {
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

  /**
   * Type for composing components which life-cycle are managed by Dagger.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @dagger.Component.Builder
  interface Build extends Builder.Part1<Build, Web, Mode>,
                          Builder.Dep1<Build, Web, Router.Build> {
  }
}
