package template;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Application test case.
 */
final class ApplicationTest {

  /**
   * Performs a sanity test with a reasonable startup time.
   *
   * <pre>{@code
   * GIVEN instant before startup
   * WHEN run sanity startup
   * THEN startup time is lesser than one second.
   * }</pre>
   */
  @Test
  @Order(1)
  @DisplayName(""
      + "GIVEN instant before startup \\n"
      + "WHEN run sanity startup \\n"
      + "THEN startup time is lesser than one second.")
  final void givenInstantBeforeStartup_whenRunSanityStartup_thenStartupTimeIsLesserThanOneSecond() {
    // Assert
    Assertions.assertDoesNotThrow(() -> {
      // Arrange
      val now = Instant.now();
      // Act
      Application.main("IS_TESTING=true");
      // Assert
      assertTrue(Duration.between(now, Instant.now()).toMillis() <= 1000);
    });
  }
}
