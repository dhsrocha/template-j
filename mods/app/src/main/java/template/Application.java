package template;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.annotations.ContentType;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import lombok.Value;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@UtilityClass
public class Application {

  /**
   * Application's entry point. Design purpose is just exposing a method for
   * {@code maven-exec-plugin} from terminal.
   *
   * @param args Key-value arguments written in {@code k=v} or {@code k:v}
   *             format in order to configure {@link Javalin server}'s
   *             initialization, under {@link Props pre-defined keys}. Any
   *             string that does not follow that pattern is going to be
   *             discarded.
   */
  public static void main(final String... args) {
    val props = new EnumMap<Props, String>(Props.class);
    Arrays.stream(args)
          .map(s -> s.split("[=:]"))
          .filter(s -> s.length == 2)
          .forEach(ss -> props.put(Props.valueOf(ss[0]), ss[1]));
    val app = Inner.bootstrap(props);
    log.info("Application running. [port={}]", app.server.port());
  }

  private enum Props {
    IS_TESTING, PORT
  }

  @Value
  static class Inner {

    Javalin server;

    static Inner bootstrap(final Map<Props, String> props) {
      return new Inner(Javalin.create(cfg -> {
        cfg.showJavalinBanner = !Boolean.parseBoolean(props.getOrDefault(
            Props.IS_TESTING, "false"));
        cfg.defaultContentType = ContentType.JSON;
        cfg.autogenerateEtags = Boolean.TRUE;
      }).start(Integer.parseInt(props.getOrDefault(Props.PORT, "9999"))));
    }
  }
}
