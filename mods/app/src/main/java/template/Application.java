package template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import lombok.NonNull;
import template.core.Bootstrap;
import template.core.Props;

/**
 * Application's entry point.
 * <br/>
 * Design purpose is just exposing {@link #main(String...) main method} for
 * {@code maven-exec-plugin} to be called from command-line.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Application {

  @javax.inject.Scope
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface Scope {
  }

  /**
   * Starts the application in non-testing mode.
   *
   * @param args key-value entries treated by {@link Props#from(String...)}.
   * @see Bootstrap#bootstrap(String...)
   */
  static void main(final String... args) {
    Bootstrap.bootstrap(args);
  }

  /**
   * Defines application's features.
   */
  enum Feat {
    USER,
    ADDRESS,
    ;

    public static Feat[] from(final @NonNull String args) {
      return from(args.split(","));
    }

    public static Feat[] from(final @NonNull String... args) {
      return Arrays.stream(args).map(Feat::valueOf).toArray(Feat[]::new);
    }
  }

  /**
   * Defines ways that application should behave.
   */
  @lombok.Getter
  @lombok.AllArgsConstructor
  enum Mode {
    /**
     * Development mode.
     */
    DEV(Boolean.FALSE),
    /**
     * Production mode.
     */
    PRD(Boolean.FALSE),
    /**
     * Test mode. Should be used under test context (surefire) only.
     */
    TEST(System.getProperties().keySet().stream().map(String::valueOf)
               .noneMatch(s -> s.contains("test") || s.contains("surefire"))),
    ;
    private final boolean forbidden;
  }

  interface Server {

    Server start(final int port);

    void stop();
  }
}
