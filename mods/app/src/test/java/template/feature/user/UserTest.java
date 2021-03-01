package template.feature.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

final class UserTest {

  @ParameterizedTest
  @CsvSource({"some_name,0",})
  @DisplayName("Should throw IllegalArgumentException due to invalid values.")
  final void shouldThrow_dueToInvalidValues(final String name, final int age) {
    // Assert / Act
    assertThrows(IllegalArgumentException.class, () -> User.of(name, age));
  }

  @Test
  @DisplayName("Should create instance with valid values.")
  final void shouldCreateInstance_withValidValues() {
    // Arrange
    val name = "some_name";
    val age = 1;
    // Assert
    val result = User.of(name, age);
    // Act
    assertEquals(name, result.getName());
    assertEquals(age, result.getAge());
  }
}

