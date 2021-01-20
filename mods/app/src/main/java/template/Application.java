package template;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.annotations.ContentType;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
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
    val map = Props.from(args);
    log.info("Properties: {}", map);
    val app = Inner.bootstrap(map);
    log.info("Application running. [port={}]", app.server.port());
  }

  @AllArgsConstructor
  enum Props {
    /**
     * Determines if the execution is under testing.
     */
    IS_TESTING("true"),
    /**
     * Application's running port.
     */
    PORT("9999"),
    ;
    private static final Pattern SPLIT = Pattern.compile("[:=]");
    private final String val;

    /**
     * Serialize input entries according to the enumerated items.
     *
     * @param args Key-value entries written in {@code k=v} or {@code k:v} under
     *             {@link Props pre-defined keys}. Any string that does not
     *             follow that pattern is going to be discarded.
     * @return Map of properties with their corresponding captured or default
     *     values if not found.
     */
    static Map<Props, String> from(final String... args) {
      val m = new EnumMap<Props, String>(Props.class);
      for (val p : values()) {
        m.put(p, p.val);
      }
      for (val s : args) {
        val ss = SPLIT.split(s, -1);
        m.put(Props.valueOf(ss[0]), ss[1]);
      }
      return m;
    }
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
