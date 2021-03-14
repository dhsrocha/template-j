package template.infra;

import io.javalin.Javalin;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

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
