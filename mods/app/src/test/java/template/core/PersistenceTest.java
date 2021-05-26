package template.core;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import template.Application.Mode;

@DisplayName("Persistence module test suite.")
class PersistenceTest {

  @Test
  @DisplayName(""
      + "GIVEN valid components "
      + "WHEN instantiating a dao component "
      + "THEN should be not null.")
  void givenValidComponents_whenInstantiatingDaoComponent_thenShouldNotBeNull() {
    // Arrange
    final var modeStub = Mode.TEST;
    final var dsStub = new HikariDataSource();
    // Act
    final var dao = Persistence.Mod.dao(modeStub, dsStub);
    // Assert
    Assertions.assertNotNull(dao);
  }

  @Test
  @DisplayName(""
      + "GIVEN null parameter"
      + "WHEN instantiating config class "
      + "THEN expect NullPointerException thrown.")
  final void givenNullParameter_whenInstantiatingConfigClass_thenExpectNpeThrown() {
    // Arrange
    final var driver = "driver";
    final var url = "url";
    final var user = "user";
    final var pwd = "pwd";
    // Act
    final var cfg = Assertions.assertDoesNotThrow(
        () -> Persistence.Config.builder()
                                .driver(driver).url(url).user(user).pwd(pwd)
                                .build());
    // Assert
    Assertions.assertEquals(driver, cfg.getDriver());
    Assertions.assertEquals(url, cfg.getUrl());
    Assertions.assertEquals(user, cfg.getUser());
    Assertions.assertEquals(pwd, cfg.getPwd());
  }
}
