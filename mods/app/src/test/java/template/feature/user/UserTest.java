package template.feature.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import template.base.stereotype.Domain.Violation;

/**
 * User test suite.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@DisplayName("User domain test suite.")
final class UserTest {

  @CsvSource({
      "AGE_ABOVE_ZERO, some_name, 0",
      "NAME_NOT_BLANK, ''       , 1"
  })
  @ParameterizedTest(name = "{0}: name: ''{1}'' age: ''{2}'' ")
  @DisplayName(""
      + "GIVEN invalid values "
      + "WHEN instantiating User object "
      + "THEN expect IllegalArgumentException thrown "
      + "AND corresponding message.")
  final void shouldThrow_dueToInvalidValues(final String msg, final String name,
                                            final int age) {
    // Assert / Act
    val ex = assertThrows(Violation.class, () -> User.of(name, age));
    assertEquals(msg, ex.getViolated().name());
  }

  @Test
  @DisplayName(""
      + "GIVEN valid values "
      + "WHEN instantiating User object "
      + "THEN returns input values.")
  final void shouldCreateInstance_withValidValues() {
    // Arrange
    val name = "some_name";
    val age = 1;
    // Assert
    val result = assertDoesNotThrow(() -> User.of(name, age));
    // Act
    assertEquals(name, result.getName());
    assertEquals(age, result.getAge());
  }

  @Test
  @DisplayName("Should order accordingly.")
  final void shouldOrderAccordingly() {
    // Arrange
    val u1 = User.of("name2", 2);
    val u2 = User.of("name1", 3);
    val u3 = User.of("name3", 1);
    // Assert / Act
    Assertions.assertEquals(-1, u1.compareTo(u2));
    Assertions.assertEquals(1, u2.compareTo(u3));
    Assertions.assertEquals(-1, u3.compareTo(u1));
  }
}
