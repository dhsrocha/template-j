package template.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import template.Application;
import template.Support;
import template.Support.IntegrationTest;

/**
 * Application's test case.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@IntegrationTest
@DisplayName("Application's smoke test cases.")
final class ApplicationTest {

  @Test
  @DisplayName(""
      + "WHEN starting up with default options "
      + "THEN expect does not throw any exception.")
  final void withDefaultOpts() {
    // Assert
    Assertions.assertDoesNotThrow(() -> Application.main(
        Props.PORT.is(Support.nextAvailablePort())));
  }
}
