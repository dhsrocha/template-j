package template;

import static template.base.Exceptions.ILLEGAL_ARGUMENT;

import java.util.Objects;
import lombok.val;
import org.slf4j.LoggerFactory;
import template.Application.Feat;
import template.Application.Mode;

/**
 * Application's bootstrap.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
interface Bootstrap {

  /**
   * Parses provided arguments and initiates application.
   *
   * @param args key-value entries treated by {@link Props#from(String...)}.
   * @see Application#main(String...)
   */
  static void bootstrap(final String... args) {
    val log = LoggerFactory.getLogger(Application.class);
    val props = Props.from(args);
    log.info("Properties:");
    props.forEach((p, v) -> log.info("* {}: [{}]", p.getKey(), v));
    val m = Mode.valueOf(props.get(Props.MODE).toUpperCase());
    ILLEGAL_ARGUMENT.throwIf(IllegalArgumentException::new, m::isForbidden);
    val feats = Feat.from(props.get(Props.FEAT));
    val port = Integer.parseInt(props.get(Props.PORT));
    // TODO System properties
    val router = DaggerRouter.builder().part1(feats).build();
    val web = DaggerWeb.builder().part1(m).dep1(router).build();
    val server = web.get().start(port);
    if (Objects.requireNonNull(server.server()).getStarted()) {
      log.info("Application running. [port={}]", port);
      Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
  }
}
