package template;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

/**
 * Indexes and parses system properties used in this application.
 */
@Getter
@AllArgsConstructor
enum Props {
  /**
   * Determines if the execution is under testing.
   */
  IS_TESTING("app.test", "true"),
  /**
   * Application's running port.
   */
  PORT("app.port", "9999"),
  ;
  private static final Props[] VALUES = values();
  private static final Pattern SPLIT = Pattern.compile("[:=]");
  private final String key;
  private final String defaultVal;

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
    if (args.length > VALUES.length) {
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
    for (val p : VALUES) {
      m.put(p, System.getProperty(p.key, aa.getOrDefault(p.key, p.defaultVal)));
    }
    return m;
  }
}
