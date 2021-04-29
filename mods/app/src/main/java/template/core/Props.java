package template.core;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.val;
import template.Application.Feat;
import template.Application.Mode;

/**
 * Indexes and parses system properties used in this application.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@lombok.Getter
@lombok.AllArgsConstructor
public enum Props {
  /**
   * Determines if the execution is under testing.
   */
  MODE("app.mode", Mode.DEV.name().toLowerCase()),
  /**
   * Application's feature profiles.
   */
  FEAT("app.feats", Arrays.toString(Feat.values()).replaceAll("[\\[\\] ]", "")),
  /**
   * Application's running port.
   */
  PORT("app.port", "9999"),
  ;
  private static final Props[] VALUES = values();
  private static final Pattern SPLIT = Pattern.compile("=");
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
  public static Map<Props, String> from(final String... args) {
    if (args.length > VALUES.length) {
      throw new IllegalArgumentException(
          "Arguments given amount is greater than the ones can be afforded!");
    }
    val aa = new HashMap<String, String>();
    for (val ss : args) {
      val s = SPLIT.split(ss, -1);
      if (s.length == 2) {
        aa.put(s[0], s[1]);
      }
    }
    val m = new EnumMap<Props, String>(Props.class);
    for (val p : VALUES) {
      m.put(p, System.getProperty(p.key, aa.getOrDefault(p.key, p.defaultVal)));
    }
    return m;
  }

  /**
   * Indicates a string value to index to the given key in a proper way.
   *
   * @param value A value to index to.
   * @return A resulting "key=value" string.
   */
  public final String is(final @lombok.NonNull Object value) {
    return key + "=" + value;
  }
}
