package template;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Test case for sake of covering {@link Application#main(String...)}.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@DisplayName("Start up with no optional configuration.")
final class MainTest {

  @Test
  @DisplayName(""
      + "WHEN starting up with default options "
      + "THEN expect does not throw any exception.")
  final void withDefaultOpts() {
    // Assert
    Assertions.assertDoesNotThrow((Executable) Application::main);
  }
}
