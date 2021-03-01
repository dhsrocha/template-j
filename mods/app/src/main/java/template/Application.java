package template;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.annotations.ContentType;
import java.util.Map;
import lombok.Value;
import lombok.val;
import org.slf4j.LoggerFactory;

/**
 * Application main class.
 */
public interface Application {

  /**
   * Application's entry point. Design purpose is just exposing a method for
   * {@code maven-exec-plugin} from terminal.
   *
   * @param args key-value entries treated by {@link Props#from(String...)}.
   */
  static void main(final String... args) {
    val log = LoggerFactory.getLogger(Application.class);
    val props = Props.from(args);
    log.info("Properties:");
    props.forEach((p, v) -> log.info("* {}: {}", p.getKey(), v));
    val app = Inner.bootstrap(props);
    log.info("Application running. [port={}]", app.server.port());
  }

  @Value
  class Inner {

    Javalin server;

    static Inner bootstrap(final Map<Props, String> mm) {
      return new Inner(Javalin.create(cfg -> {
        cfg.showJavalinBanner = !Boolean.parseBoolean(mm.get(Props.IS_TESTING));
        cfg.defaultContentType = ContentType.JSON;
        cfg.autogenerateEtags = Boolean.TRUE;
      }).start(Integer.parseInt(mm.get(Props.PORT))));
    }
  }
}
