package dhsrocha;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.annotations.ContentType;
import java.util.Properties;
import lombok.Value;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@UtilityClass
public class Application {

  private static final String IS_TESTING = "isTesting";

  /**
   * Application's entry point. Design purpose is just exposing a method for
   * maven-exec-plugin from terminal.
   *
   * @param args Method's arguments. No use for while.
   */
  public static void main(final String... args) {
    log.info("Application running.");
    val props = new Properties();
    props.put(IS_TESTING, Boolean.FALSE);
    Inner.bootstrap(props).getServer().start(7000);
  }

  @Value
  static class Inner {

    Javalin server;

    static Inner bootstrap(final Properties props) {
      val server = Javalin.create(cfg -> {
        cfg.showJavalinBanner = !Boolean
            .parseBoolean(props.getProperty(IS_TESTING));
        cfg.defaultContentType = ContentType.JSON;
        cfg.autogenerateEtags = Boolean.TRUE;
      });
      if (Boolean.parseBoolean(props.getProperty(IS_TESTING))) {
        server.start(Integer.parseInt(props.getProperty("port", "9999")));
      }
      return new Inner(server);
    }
  }
}
