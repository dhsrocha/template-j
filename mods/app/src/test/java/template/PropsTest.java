package template;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Test case for application input parser.")
final class PropsTest {

  @CsvSource({
      "app.test:false, false, IS_TESTING",
      "app.port:0000, 0000, PORT"
  })
  @ParameterizedTest
  @DisplayName("Success test cases.")
  final void success(final String input, final String val, final String name) {
    // Arrange
    val p = Props.valueOf(name);
    // Act
    val result = Props.from(input);
    // Assert
    assertEquals(val, result.get(p));
  }

  @CsvSource({
      "app.test:=1111",
      "a:a:a",
      "a:a:a:a"
  })
  @ParameterizedTest
  @DisplayName("Values ignored and default values returned.")
  final void ignore(final String input) {
    // Act
    val props = Props.from(input);
    // Assert
    assertEquals(Props.IS_TESTING.getVal(), props.get(Props.IS_TESTING));
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
