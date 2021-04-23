package template.feature.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import template.base.stereotype.Domain.Invariant;
import template.base.stereotype.Domain.Violation;

/**
 * User test suite.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@DisplayName("User domain test suite.")
final class UserTest {

  @ParameterizedTest(name = "{0}: name: ''{1}'' age: ''{2}'' ")
  @MethodSource("invalidArgs")
  @DisplayName(""
      + "GIVEN invalid values "
      + "WHEN instantiating User object "
      + "THEN expect IllegalArgumentException thrown "
      + "AND corresponding message.")
  final void shouldThrow_dueToInvalidValues(final List<String> rules,
                                            final String name,
                                            final int age) {
    // Act
    val ex = assertThrows(Violation.class, () -> User.of(name, age));
    // Assert
    assertTrue(rules.containsAll(ex.getInvariants().stream()
                                   .map(Invariant::name)
                                   .collect(Collectors.toSet())));
  }

  @SuppressWarnings("unused")
  private static Stream<Arguments> invalidArgs() {
    return Stream.of(
        arguments(List.of("AGE_ABOVE_ZERO"), "some_name", 0),
        arguments(List.of("NAME_NOT_BLANK"), "", 1),
        arguments(List.of("AGE_ABOVE_ZERO", "NAME_NOT_BLANK"), "", 0));
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
