package template;

import dagger.BindsInstance;
import dagger.Component;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.plugin.openapi.annotations.ContentType;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.val;
import org.slf4j.LoggerFactory;
import template.Application.Web;
import template.base.contract.Builder;

/**
 * Application's entry point.
 * <br/>
 * Design purpose is just exposing {@link #main(String...) main method} for
 * {@code maven-exec-plugin} to be called from command-line.
 */
@Singleton
@Component(modules = Router.class)
public interface Application extends Supplier<Web> {

  /**
   * Parses provided arguments and initiates application.
   *
   * @param args key-value entries treated by {@link Props#from(String...)}.
   */
  static void main(final String... args) {
    val log = LoggerFactory.getLogger(Application.class);
    val props = Props.from(args);
    log.info("Properties:");
    props.forEach((p, v) -> log.info("* {}: [{}]", p.getKey(), v));
    val mode = Mode.valueOf(props.get(Props.MODE).toUpperCase());
    val feats = Feat.from(props.get(Props.FEAT));
    val port = Integer.parseInt(props.get(Props.PORT));
    val app = DaggerApplication.builder().mode(mode).features(feats).build();
    val server = app.get().bootstrap(port);
    if (Objects.requireNonNull(server.server()).getStarted()) {
      log.info("Application running. [port={}]", server.port());
      Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
  }

  /**
   * Defines application's features.
   */
  enum Feat {
    DEFAULT; // Just a placeholder

    static Feat[] from(final @NonNull String... args) {
      return Arrays.stream(args).map(Feat::valueOf).toArray(Feat[]::new);
    }
  }

  /**
   * Defines ways that application should behave.
   */
  enum Mode {
    /**
     * Development mode.
     */
    DEV,
    /**
     * Production mode.
     */
    PRD,
  }

  @Component.Builder
  interface Build extends Builder<Application> {

    @BindsInstance
    Build mode(final @NonNull Mode mode);

    @BindsInstance
    Build features(final @NonNull Feat[] features);
  }

  /**
   * Application initialization and bootstrap.
   */
  final class Web {

    private final @NonNull Mode mode;
    private final @NonNull Router routes;

    @Inject
    Web(final Mode mode, final Router routes) {
      this.mode = mode;
      this.routes = routes;
    }

    Javalin bootstrap(final int port) {
      return Javalin.create(cfg -> {
        cfg.showJavalinBanner = mode != Mode.DEV;
        cfg.defaultContentType = ContentType.JSON;
        cfg.autogenerateEtags = Boolean.TRUE;
      }).routes(routes).start(port);
    }
  }
}
