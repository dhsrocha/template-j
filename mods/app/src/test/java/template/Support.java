package template;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.val;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import template.Application.Feat;
import template.Application.Mode;
import template.core.Bootstrap;
import template.core.Props;

/**
 * Supporting assets for testing purposes.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Support {

  /**
   * Activates {@link Bootstrap#bootstrap(String...)} to test endpoints under
   * <b>integration testing strategy</b>. Must be used along with
   * {@link Client}.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @ExtendWith({AppExtension.class})
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @interface IntegrationTest {
    /**
     * Features to activate on application startup. Default value means
     * loading up {@link Feat#values() all features available}. Values added
     * to {@link #activated()} method have higher topmost priority.
     *
     * @return Feature entries.
     * @see #activated()
     */
    Feat[] value() default {};

    /**
     * Features to activate on application startup. Default value means
     * loading up {@link Feat#values() all features available}. Values added
     * here have higher topmost priority.
     *
     * @return Feature entries.
     */
    Feat[] activated() default {};
  }

  /**
   * Common listening port shared by the starting up application and its
   * requesting clients.
   */
  int PORT = nextAvailablePort();

  /**
   * Recursively loops until finding a available TCP port, locks it, get its
   * value and then releases it.
   *
   * @return next available port.
   */
  private static int nextAvailablePort() {
    try (val s = new ServerSocket(0)) {
      return s.getLocalPort();
    } catch (final IOException e) {
      return nextAvailablePort();
    }
  }

  /**
   * JUnit extension to start up {@link Bootstrap application's bootstrap}.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  final class AppExtension implements BeforeTestExecutionCallback,
                                      AfterTestExecutionCallback {
    private static final AtomicReference<Application.Server> REF =
        new AtomicReference<>();

    @Override
    public void beforeTestExecution(final ExtensionContext ctx) {
      // Setup
      val suite = ctx.getTestInstance().orElseThrow().getClass()
                     .getAnnotation(IntegrationTest.class);
      val feats = Optional
          .of(suite.activated()).filter(l -> l.length > 0)
          .or(() -> Optional.of(suite.value()).filter(l -> l.length > 0))
          .orElseGet(Feat::values);
      REF.set(Bootstrap.bootstrap(
          Props.MODE.is(Mode.TEST),
          Props.PORT.is(Support.PORT),
          Props.FEAT.is(Arrays.toString(feats).replaceAll("[\\[\\] ]", ""))));
    }

    @Override
    public void afterTestExecution(final ExtensionContext ctx) {
      REF.get().stop();
    }
  }
}
