package template.core;

import lombok.val;
import org.slf4j.LoggerFactory;
import template.Application;
import template.Application.Feat;
import template.Application.Mode;
import template.base.Exceptions;

/**
 * Application's bootstrap.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Bootstrap {

  /**
   * Parses provided arguments and initiates application0s modules.
   *
   * @param args key-value entries treated by {@link Props#from(String...)}.
   * @see Application#main(String...)
   */
  static Application.Server bootstrap(final String... args) {
    val log = LoggerFactory.getLogger(Application.class);
    val props = Props.from(args);
    log.debug("Properties:");
    props.forEach((p, v) -> log.debug("* {}: [{}]", p.getKey(), v));
    val m = Mode.valueOf(props.get(Props.MODE).toUpperCase());
    Exceptions.ILLEGAL_ARGUMENT.throwIf(m::isForbidden);
    val feats = Feat.from(props.get(Props.FEAT));
    val port = Integer.parseInt(props.get(Props.PORT));
    val cfg = Persistence.Config.builder()
                                .driver(props.get(Props.DB_DRIVER))
                                .url(props.get(Props.DB_URL))
                                .user(props.get(Props.DB_USER))
                                .pwd(props.get(Props.DB_PWD))
                                .build();
    val dao = DaggerPersistence.builder().part1(m).part2(cfg).build().get();
    val router = DaggerRoutes.builder().part1(m).part2(feats).part3(dao);
    val server = DaggerWeb.builder().part1(m).dep1(router).build().get();
    return server.start(port);
  }
}
