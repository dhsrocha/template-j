package template;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.val;
import org.slf4j.LoggerFactory;

public interface Main {

  /**
   * Builds middleware assets according to a given environment. Main purpose is
   * to create or refresh all required resources for running an application.
   *
   * @param args key-value entries treated by {@link Props#from(String...)}.
   */
  static void main(final String[] args) {
    val log = LoggerFactory.getLogger(Main.class);
    val props = Props.from(args);
    log.info("Properties:");
    props.forEach((p, v) -> log.info("* {}: {}", p.key, v));
    val host = Client.host(Boolean.parseBoolean(props.get(Props.DEV_MODE)));
    host.swarmMode(Integer.parseInt(props.get(Props.WORKERS)));
    host.servicesFrom(Middleware.stream(props.get(Props.SERVICES)));
  }

  @AllArgsConstructor
  enum Props {
    /**
     * Indicates if the build is going to work in a development environment.
     * Defaults to {@code false}.
     */
    DEV_MODE("middleware.dev", "false"),
    /**
     * Indicates the pod's name. Defaults to project's root folder.
     */
    POD_NAME("middleware.pod",
             Path.of("").toAbsolutePath().toFile().getName()),
    /**
     * All middlewares should be activated if any is not sent.
     */
    SERVICES("middleware.services", EnumSet
        .allOf(Middleware.class).stream().map(Enum::name)
        .collect(Collectors.joining(","))),
    /**
     * Number or workers for the cluster.
     */
    WORKERS("middleware.workers", "3"),
    ;
    private static final Pattern SPLIT = Pattern.compile("[:=]");
    private final String key;
    private final String val;

    /**
     * Serialize input entries according to the enumerated items.
     *
     * @param args Key-value entries written in {@code k=v} or {@code k:v} under
     *             {@link Props pre-defined keys}. Any string that does not
     *             follow that pattern is going to be discarded. Main purpose is
     *             to be used in test context.
     * @return Map of properties with the following values:
     *     <ul>
     *       <li>Corresponding captured value;</li>
     *       <li>Input from system/command-line; or</li>
     *       <li>Pre-defined values.</li>
     *     </ul>
     * @throws IllegalArgumentException if number of arguments is grater than
     *                                  the enum {@link #values()}.
     */
    static Map<Props, String> from(final String... args) {
      val values = values();
      if (args.length > values.length) {
        throw new IllegalArgumentException(
            "Arguments given amount is greater than the ones can be afforded!");
      }
      val aa = new HashMap<String, String>();
      for (val ss : args) {
        val s = SPLIT.split(ss, -1);
        if (s.length == 2) {
          aa.putIfAbsent(s[0], s[1]);
        }
      }
      val m = new EnumMap<Props, String>(Props.class);
      for (val p : values) {
        m.put(p, System.getProperty(p.key, aa.getOrDefault(p.key, p.val)));
      }
      return m;
    }
  }
}
