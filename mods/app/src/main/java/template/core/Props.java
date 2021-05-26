package template.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
   *
   * @see Mode
   */
  MODE("mode", Mode.DEV.name().toLowerCase()),
  /**
   * Application's {@link Feat feature} profiles.
   *
   * @see Feat
   */
  FEAT("feats", Arrays.toString(Feat.values()).replaceAll("[\\[\\] ]", "")),
  /**
   * Application's running port.
   */
  PORT("port", "9999"),
  // ::: DB settings :::
  /**
   * Application's running port.
   */
  DB_DRIVER("db.driver", null),
  /**
   * Database URL to connect to. <b>Not provided but required</b>.
   */
  DB_URL("db.url", null),
  /**
   * Database username to connect to. <b>Not provided but required</b>.
   */
  DB_USER("db.user", null),
  /**
   * Database password to connect to. <b>Not provided but required</b>.
   */
  DB_PWD("db.pwd", null),
  ;
  private static final Props[] VALUES = values();
  private static final Pattern SPLIT = Pattern.compile("=");
  private static final Path ROOT = Paths
      .get(Objects.requireNonNull(Props.class.getResource("")).getPath())
      .getParent().getParent().getParent().getParent().getFileName();

  private final String key;
  private final String defaultVal;

  /**
   * Serialize input entries according to the enumerated items.
   *
   * @param args Key-value entries written in {@code k=v} or {@code k:v} under
   *             {@link Props pre-defined keys}. Any string that does not follow
   *             that pattern is going to be discarded. Main purpose is to be
   *             used in test context.
   * @return Map of properties with the following values:
   *     <ul>
   *       <li>Corresponding captured value;</li>
   *       <li>Input from system/command-line; or</li>
   *       <li>Pre-defined values.</li>
   *     </ul>
   * @throws IllegalArgumentException if number of arguments is grater than the
   *                                  enum {@link #values()}.
   */
  public static Map<Props, String> from(final @lombok.NonNull String... args) {
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
      m.put(p, System.getProperty(p.getKey(),
                                  aa.getOrDefault(p.getKey(), p.defaultVal)));
    }
    return m;
  }

  /**
   * Indicates a string value to index to the given key in a proper way.
   *
   * @param value A value to index to.
   * @return A resulting {@code key=value} string.
   */
  public final String is(final @lombok.NonNull Object value) {
    return getKey() + "=" + value;
  }

  /**
   * Retrieves the {@link Props} key.
   *
   * @return Key entry appended with project's containing folder.
   */
  public final String getKey() {
    return ROOT + "." + key;
  }
}
