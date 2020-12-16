package dhsrocha;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Application test case.
 */
final class ApplicationTest {

  /**
   * Integration test, sanity check: should start without breaking.
   */
  @Test
  @DisplayName("Integration test, sanity check: should start without breaking.")
  final void sanityTest_shouldNotBreak() {
    // Act / Assert
    Assertions.assertDoesNotThrow((Executable) Application::main);
  }
}
