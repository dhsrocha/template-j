package template;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import template.Application.Mode;

/**
 * Application's test case.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Slf4j
@DisplayName("Application's smoke test cases.")
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
  @DisplayName(""
      + "GIVEN instant before startup "
      + "WHEN run sanity startup "
      + "THEN startup time is lesser than one second.")
  final void givenInstantBeforeStartup_whenRunSanityStartup_thenStartupTimeIsLesserThanOneSecond() {
    // Assert
    Assertions.assertDoesNotThrow(() -> {
      // Arrange
      val now = Instant.now();
      // Act
      Application.main(Props.MODE.getKey() + "=" + Mode.TEST);
      // Assert
      val elapsed = Duration.between(now, Instant.now()).toMillis();
      log.info("Startup time: [{}]", elapsed);
      assertTrue(elapsed <= 1000);
    });
  }
}
