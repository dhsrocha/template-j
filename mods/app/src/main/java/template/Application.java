package template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import javax.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Application's entry point.
 * <br/>
 * Design purpose is just exposing {@link #main(String...) main method} for
 * {@code maven-exec-plugin} to be called from command-line.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">>Diego Rocha</a>
 */
@Singleton
@dagger.Component
public interface Application {

  @javax.inject.Scope
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface Scope {
  }

  /**
   * Starts the application in non-testing mode.
   *
   * @param args key-value entries treated by {@link Props#from(String...)}.
   * @see Bootstrap#bootstrap(boolean, String...)
   */
  static void main(final String... args) {
    Bootstrap.bootstrap(Boolean.FALSE, args);
  }

  /**
   * Defines application's features.
   */
  enum Feat {
    DEFAULT, // Just a placeholder
    ;

    static Feat[] from(final @NonNull String... args) {
      return Arrays.stream(args).map(Feat::valueOf).toArray(Feat[]::new);
    }
  }

  /**
   * Defines ways that application should behave.
   */
  @Getter
  @AllArgsConstructor
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
     * Test mode. Should be used internally only.
     */
    TEST(Boolean.TRUE),
    ;
    private final boolean isInternalOnly;
  }
}
