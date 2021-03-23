package template;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Application input parser's test case.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@DisplayName("Application's input parser test suite.")
final class PropsTest {

  @CsvSource({
      "app.mode=dev, dev, MODE",
      "app.port=0000, 0000, PORT",
      "app.feat=, DEFAULT, FEAT"
  })
  @ParameterizedTest
  @DisplayName("Successful test cases.")
  final void success(final String input, final String val, final String name) {
    // Arrange
    val p = assertDoesNotThrow(() -> Props.valueOf(name));
    // Act
    val result = assertDoesNotThrow(() -> Props.from(input));
    // Assert
    assertEquals(val, result.get(p));
  }

  @CsvSource({
      "app.port:=1111",
      "a:a:a",
      "a:a:a:a"
  })
  @ParameterizedTest
  @DisplayName("Values ignored and default values returned.")
  final void ignore(final String input) {
    // Act
    val props = assertDoesNotThrow(() -> Props.from(input));
    // Assert
    assertEquals(Props.MODE.getDefaultVal(), props.get(Props.MODE));
  }

  @Test
  @DisplayName("Input length has more than enum's items length.")
  final void differentLength() {
    // Arrange
    val invalid = "a,".repeat(Props.values().length + 1).split(",");
    // Assert - Act
    assertThrows(IllegalArgumentException.class, () -> Props.from(invalid));
  }
}
