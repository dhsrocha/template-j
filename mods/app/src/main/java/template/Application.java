package template;

import dagger.Component;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Objects;
import javax.inject.Singleton;
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
@Component
public interface Application {

  @javax.inject.Scope
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface Scope {
  }

  /**
   * Parses provided arguments and initiates application.
   *
   * @param args key-value entries treated by {@link Props#from(String...)}.
   */
  static void main(final String... args) {
    val log = LoggerFactory.getLogger(Application.class);
    val props = Props.from(args);
    log.info("Properties:");
    props.forEach((p, v) -> log.info("* {}: [{}]", p.getKey(), v));
    val mode = Mode.valueOf(props.get(Props.MODE).toUpperCase());
    val feats = Feat.from(props.get(Props.FEAT));
    val port = Integer.parseInt(props.get(Props.PORT));
    // TODO System properties
    val router = DaggerRouter.builder().part1(feats).build();
    val web = DaggerWeb.builder().part1(mode).part2(router.get()).build();
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
  enum Mode {
    /**
     * Development mode.
     */
    DEV,
    /**
     * Production mode.
     */
    PRD,
  }
}
