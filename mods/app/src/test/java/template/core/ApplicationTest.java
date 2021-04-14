package template.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import template.Application;
import template.Application.Mode;
import template.Support;
import template.Support.DbExtension;
import template.Support.IntegrationTest;

/**
 * Application's test suite.
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
    Assertions.assertDoesNotThrow(
        // Act
        () -> Application.main(Props.MODE.is(Mode.TEST),
                               Props.PORT.is(Support.nextAvailablePort()),
                               Props.DB_DRIVER.is(org.hsqldb.jdbcDriver.class
                                                      .getCanonicalName()),
                               Props.DB_URL.is(DbExtension.DB_URL),
                               Props.DB_USER.is(DbExtension.DB_USER),
                               Props.DB_PWD.is(DbExtension.DB_PWD)));
  }
}
