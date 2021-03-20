package template.infra;

import dagger.Module;
import dagger.Provides;
import io.javalin.Javalin;
import io.javalin.plugin.openapi.annotations.ContentType;
import lombok.NonNull;
import template.Application.Mode;
import template.base.contract.Routes;
import template.infra.Infra.InfraScope;

/**
 * Module for bootstrapping application's web server.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">>Diego Rocha</a>
 */
@Module
interface Web {

  @Provides
  @InfraScope
  static Javalin server(final @NonNull Mode mode,
                        final @NonNull Routes routes) {
    return Javalin.create(cfg -> {
      cfg.showJavalinBanner = mode != Mode.DEV;
      cfg.defaultContentType = ContentType.JSON;
      cfg.autogenerateEtags = Boolean.TRUE;
    }).routes(routes);
  }
}
