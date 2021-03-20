package template.infra;

import static org.mockito.Mockito.mock;

import io.javalin.Javalin;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Infrastructure component's test suite.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">>Diego Rocha</a>
 */
@DisplayName("Test suite for infrastructure bootstrap.")
final class InfraTest {

  @Test
  @DisplayName("Should return injected webserver.")
  final void shouldReturn_injectWebserver() {
    // Arrange
    val mock = mock(Javalin.class);
    // Act
    val result = new Infra.Mod(mock);
    // Assert
    Assertions.assertEquals(mock, result.get());
  }
}
