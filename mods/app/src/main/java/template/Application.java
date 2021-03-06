package template;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.annotations.ContentType;
import java.util.Map;
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
    val app = new Bootstrap(props);
    if (app.start()) {
      log.info("Application running. [port={}]", app.server.port());
    }
    Runtime.getRuntime().addShutdownHook(new Thread(app.server::stop));
  }

  /**
   * Application initialization and bootstrap.
   */
  @Value
  class Bootstrap {

    Javalin server = Javalin.create();
    @NonNull Map<Props, String> props;

    boolean start() {
      server.config.showJavalinBanner =
          !Boolean.parseBoolean(props.get(Props.IS_TESTING));
      server.config.defaultContentType = ContentType.JSON;
      server.config.autogenerateEtags = Boolean.TRUE;
      server.start(Integer.parseInt(props.get(Props.PORT)));
      return Boolean.TRUE;
    }
  }
}
