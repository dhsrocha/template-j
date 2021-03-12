package template;

import dagger.BindsInstance;
import dagger.Component;
import io.javalin.Javalin;
import io.javalin.plugin.openapi.annotations.ContentType;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.val;
import org.slf4j.LoggerFactory;
import template.Application.Bootstrap;
import template.base.contract.Builder;

/**
 * Application's entry point.
 * <br/>
 * Design purpose is just exposing {@link #main(String...) main method} for
 * {@code maven-exec-plugin} to be called from command-line.
 */
@Singleton
@Component
public interface Application extends Supplier<Bootstrap> {

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
    val server = app.get().start(port);
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
  final class Bootstrap {

    private final Javalin server = Javalin.create();
    private final @NonNull Mode mode;
    private final @NonNull Router routes;

    @Inject
    Bootstrap(final Mode mode, final Router routes) {
      this.mode = mode;
      this.routes = routes;
    }

    Javalin start(final int port) {
      server.config.showJavalinBanner = mode != Mode.DEV;
      server.config.defaultContentType = ContentType.JSON;
      server.config.autogenerateEtags = Boolean.TRUE;
      server.routes(routes);
      return server.start(port);
    }
  }
}
