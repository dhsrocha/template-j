package template;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.annotations.ContentType;
import java.util.Objects;
import lombok.NonNull;
import lombok.Value;
import lombok.val;
import org.slf4j.LoggerFactory;

/**
 * Application's entry point.
 * <br/>
 * Design purpose is just exposing {@link #main(String...) main method} for
 * {@code maven-exec-plugin} to be called from command-line.
 */
public interface Application {

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
    val server = new Bootstrap().bootstrap(mode);
    server.start(Integer.parseInt(props.get(Props.PORT)));
    if (Objects.requireNonNull(server.server()).getStarted()) {
      log.info("Application running. [port={}]", server.port());
      Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
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

  /**
   * Application initialization and bootstrap.
   */
  @Value
  class Bootstrap {

    Javalin server = Javalin.create();

    Javalin bootstrap(final @NonNull Mode mode) {
      server.config.showJavalinBanner = mode != Mode.DEV;
      server.config.defaultContentType = ContentType.JSON;
      server.config.autogenerateEtags = Boolean.TRUE;
      return server;
    }
  }
}
