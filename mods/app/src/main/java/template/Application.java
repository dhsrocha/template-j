package template;

import static template.base.Exceptions.ILLEGAL_ARGUMENT;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Objects;
import javax.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.slf4j.LoggerFactory;

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
   * Parses provided arguments and initiates application.
   *
   * @param args key-value entries treated by {@link Props#from(String...)}.
   * @see #main(boolean, String...)
   */
  static void main(final String... args) {
    main(Boolean.FALSE, args);
  }

  /**
   * Parses provided arguments and initiates application.
   *
   * @param args     key-value entries treated by {@link Props#from(String...)}.
   * @param testMode Enables test mode. Meant to be used from test cases.
   */
  static void main(final boolean testMode, final String... args) {
    val log = LoggerFactory.getLogger(Application.class);
    val props = Props.from(args);
    log.info("Properties:");
    props.forEach((p, v) -> log.info("* {}: [{}]", p.getKey(), v));
    val m = Mode.valueOf(props.get(Props.MODE).toUpperCase());
    ILLEGAL_ARGUMENT.throwIf(IllegalArgumentException::new, m::isInternalOnly);
    val mode = testMode ? Mode.TEST : m;
    val feats = Feat.from(props.get(Props.FEAT));
    val port = Integer.parseInt(props.get(Props.PORT));
    // TODO System properties
    val router = DaggerRouter.builder().part1(feats).build();
    val web = DaggerWeb.builder().part1(mode).dep1(router).build();
    val server = web.get().start(port);
    if (Objects.requireNonNull(server.server()).getStarted()) {
      log.info("Application running. [port={}]", port);
      Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
  }

  /**
   * Defines application's features.
   */
  enum Feat {
    DEFAULT; // Just a placeholder

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
