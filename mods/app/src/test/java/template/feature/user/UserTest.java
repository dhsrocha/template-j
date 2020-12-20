package template.feature.user;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

final class UserTest {

  @ParameterizedTest
  @CsvSource({
      ",1",
      "some_name,0",
  })
  @DisplayName("Should throw IllegalArgumentException due to invalid values.")
  final void shouldThrow_dueToInvalidValues(final String name, final int age) {
    // Assert / Act
    Assertions.assertThrows(IllegalArgumentException.class, () -> User.of(name, age));
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
    Assertions.assertEquals(name, result.getName());
    Assertions.assertEquals(age, result.getAge());
  }
}

