package template;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
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

  private static final String SYS_STUB = "system stub";
  private static final String ARG_STUB = "arg stub";

  @BeforeEach
  final void setUp() {
    System.getProperties().clear();
  }

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
  @DisplayName("Should expect exception from excessive argument length.")
  final void length() {
    // Arrange
    val args = ",.".repeat(Props.values().length + 1).split(",");
    // Act / Assert
    assertThrows(IllegalArgumentException.class, () -> Props.from(args));
  }

  @Test
  @DisplayName("Should follow setting preceding order: system > arg.")
  final void settingSystemOverArg() {
    // Arrange
    System.setProperty(Props.PORT.getKey(), SYS_STUB);
    // Act
    val result = Props.from(Props.PORT.is(ARG_STUB));
    // Assert
    assertEquals(SYS_STUB, result.get(Props.PORT));
    // Arrange
    System.clearProperty(Props.PORT.getKey());
  }

  @Test
  @DisplayName("Should follow setting preceding order: arg > default.")
  final void settingArgOverDefault() {
    // Act
    val result = Props.from(Props.PORT.is(ARG_STUB));
    // Assert
    assertEquals(ARG_STUB, result.get(Props.PORT));
  }
}
