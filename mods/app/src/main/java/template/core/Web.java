package template.core;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.plugin.json.JsonMapper;
import io.javalin.plugin.openapi.annotations.ContentType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.val;
import template.Application;
import template.Application.Mode;
import template.base.contract.Buildable;
import template.base.stereotype.Domain.Violation;
import template.core.Web.Mod;

/**
 * Module for bootstrapping application's web server.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Application.Scope
@dagger.Component(dependencies = Routes.Build.class, modules = Mod.class)
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
    static Application.Server server(final @NonNull Mode mode,
                                     final @NonNull Routes.Build routes) {
      val mapper = new Gson();
      val app = Javalin.create(cfg -> {
        cfg.showJavalinBanner = mode == Mode.PRD;
        cfg.defaultContentType = ContentType.JSON;
        cfg.autogenerateEtags = Boolean.TRUE;
        cfg.jsonMapper(new JsonMapper() {

          @Override
          public @NonNull String toJsonString(final @NonNull Object o) {
            return mapper.toJson(o);
          }

          @Override
          public @NonNull <T> T fromJsonString(final @NonNull String src,
                                               final @NonNull Class<T> ref) {
            return mapper.fromJson(src, ref);
          }

          @Override
          public @NonNull InputStream toJsonStream(final @NonNull Object o) {
            return new ByteArrayInputStream(toJsonString(o).getBytes());
          }

          @Override
          public @NonNull <T> T fromJsonStream(final @NonNull InputStream src,
                                               final @NonNull Class<T> ref) {
            return mapper.fromJson(new InputStreamReader(src, StandardCharsets.UTF_8), ref);
          }
        });
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
  interface Build extends Buildable.Part1<Build, Web, Mode>,
                          Buildable.Dep1<Build, Web, Routes.Build> {
  }
}
