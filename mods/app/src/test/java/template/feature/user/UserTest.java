package template.feature.user;

import static template.feature.StubSupport.userStub;

import io.javalin.plugin.openapi.annotations.HttpMethod;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import template.Application.Feat;
import template.Client;
import template.Support.IntegrationTest;

/**
 * {@link User} feature's test suite.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("ClassCanBeStatic")
@DisplayName("User feature test suite using integration test strategy.")
final class UserTest {

  private static final Client<User> CLIENT = Client.create(User.class);
  private static final User VALID_STUB = userStub(1).findAny().orElseThrow();
  private static final Map<String, String> INVALID_STUB = Map
      .of("age", "0", "name", "some", "email", "non-email");

  @Nested
  @IntegrationTest(Feat.USER)
  @DisplayName("Create operations.")
  class Create {

    @Test
    @DisplayName(""
        + "GIVEN valid resource to persist "
        + "WHEN perform user create operation "
        + "THEN should be able to find resource.")
    final void givenValidResource_whenCreating_thenShouldAbleToFindResource() {
      // Act
      val resp = CLIENT
          .request(req -> req.method(HttpMethod.POST).body(VALID_STUB)).get();
      // Assert
      Assertions.assertEquals(201, resp.statusCode());
      val found = CLIENT
          .request(req -> req.method(HttpMethod.GET).uri(resp.body()))
          .thenTurnInto(User.class);
      Assertions.assertEquals(VALID_STUB, found);
    }
  }

  @Nested
  @IntegrationTest(Feat.USER)
  @DisplayName("Retrieve operations.")
  class Retrieve {

    @Test
    @DisplayName(""
        + "GIVEN three distinct resources "
        + "AND a request body as filtering criterion "
        + "WHEN perform retrieve operation "
        + "THEN return resources with matching attributes from request body.")
    final void given3created_andFilteringCriterion_whenRetrieve_thenReturnMatching() {
      // Arrange
      val ids = userStub(3)
          .map(u -> CLIENT.request(
              req -> req.method(HttpMethod.POST).body(u)))
          .map(req -> req.get().body())
          .toArray(String[]::new);
      val pick = ids[new Random().nextInt(ids.length)];
      val criteria = CLIENT.request(req -> req.method(HttpMethod.GET).uri(pick))
                           .thenTurnInto(User.class);
      // Act
      val filtered = CLIENT.retrieve(criteria).thenMap();
      // Assert
      Assertions.assertEquals(1, filtered.size());
    }

    @ParameterizedTest
    @CsvSource({"limit, 5, 5", "skip, 5, 10"})
    @DisplayName(""
        + "GIVEN 15 user resources created "
        + "AND parameters to skip 5 and limit 15 resources "
        + "WHEN perform user retrieve operation "
        + "THEN return expected parameter.")
    final void given15Created_andSkipLimitParams_whenRetrieve_thenReturnExpected(
        final String key, final String val, final int expected) {
      // Arrange
      userStub(15)
          .map(u -> CLIENT.request(req -> req.method(HttpMethod.POST).body(u)))
          .map(Supplier::get)
          .forEachOrdered(r -> Assertions.assertEquals(201, r.statusCode()));
      // Act
      val found = CLIENT.retrieve(Map.of(key, val)).thenMap();
      // Assert
      Assertions.assertEquals(expected, found.size());
    }

    @Test
    @DisplayName(""
        + "GIVEN three created resources "
        + "WHEN perform user retrieve operation "
        + "THEN return all resources created.")
    final void given3createdResources_whenRetrieve_thenReturnAllResourcesCreated() {
      // Arrange
      val ids = userStub(3)
          .map(u -> CLIENT.request(req -> req.method(HttpMethod.POST).body(u)))
          .map(req -> req.thenTurnInto(UUID.class))
          .sorted().toArray(UUID[]::new);
      // Act
      val found = CLIENT.retrieve().thenMap();
      // Assert
      val arr = found.keySet().stream().sorted().toArray(UUID[]::new);
      Assertions.assertArrayEquals(ids, arr);
    }
  }

  @Nested
  @IntegrationTest(Feat.USER)
  @DisplayName("Update operations.")
  class Update {

    @Test
    @DisplayName(""
        + "GIVEN a created resource "
        + "WHEN perform user update operation "
        + "THEN return true.")
    final void givenCreatedResource_whenUpdate_thenReturnTrue() {
      // Arrange
      val created = CLIENT
          .request(req -> req.method(HttpMethod.POST).body(VALID_STUB))
          .thenTurnInto(UUID.class);
      val toUpdate = User.of("updated", "updated@updated.com", "updated", 5);
      // Act
      val isUpdated = CLIENT.request(
          req -> req.method(HttpMethod.PATCH).uri(created).body(toUpdate))
                            .get();
      // Assert
      Assertions.assertEquals(204, isUpdated.statusCode());
      val found = CLIENT.request(req -> req.method(HttpMethod.GET).uri(created))
                        .thenTurnInto(User.class);
      Assertions.assertEquals(toUpdate, found);
    }
  }

  @Nested
  @IntegrationTest(Feat.USER)
  @DisplayName("Delete operations.")
  class Delete {

    @Test
    @DisplayName(""
        + "GIVEN a created resource "
        + "WHEN perform user delete operation "
        + "THEN return true.")
    final void givenCreatedResource_whenDelete_thenReturnTrue() {
      // Arrange
      val created = CLIENT
          .request(req -> req.method(HttpMethod.POST).body(VALID_STUB))
          .thenTurnInto(UUID.class);
      // Act
      val isDeleted = CLIENT
          .request(req -> req.method(HttpMethod.DELETE).uri(created)).get();
      // Assert
      Assertions.assertEquals(204, isDeleted.statusCode());
      val notFound = CLIENT
          .request(req -> req.method(HttpMethod.GET).uri(created)).get();
      Assertions.assertEquals(404, notFound.statusCode());
    }
  }

  @Nested
  @IntegrationTest(Feat.USER)
  @DisplayName("Bad requests.")
  class BadRequest {

    @Test
    @DisplayName(""
        + "GIVEN invalid request "
        + "WHEN perform user creation operation "
        + "THEN return 422 as HTTP status code.")
    final void givenInvalidRequest_whenCreate_thenReturn422asStatus() {
      // Act
      val resp = CLIENT
          .request(req -> req.method(HttpMethod.POST).body(INVALID_STUB)).get();
      // Assert
      Assertions.assertEquals(422, resp.statusCode());
    }

    @Test
    @DisplayName(""
        + "GIVEN invalid filter query "
        + "WHEN perform user retrieve operation "
        + "THEN return 400 as HTTP status code.")
    final void givenInvalidFilterQuery_whenRetrieve_thenReturn400asStatus() {
      // Arrange
      val params = Map.of("fq", "xp");
      // Act
      val resp = CLIENT.retrieve(params).get();
      // Assert
      Assertions.assertEquals(400, resp.statusCode());
    }

    @ParameterizedTest
    @EnumSource(value = HttpMethod.class, names = {"POST", "PATCH"})
    @DisplayName(""
        + "GIVEN empty request body "
        + "WHEN perform user create or update operation "
        + "THEN return 400 as HTTP status code.")
    final void givenEmptyBody_whenCreatingUpdate_thenReturn400asStatus(
        final @NonNull HttpMethod m) {
      // Arrange
      val fake = HttpMethod.PATCH == m ? UUID.randomUUID().toString() : "";
      // Act
      val resp = CLIENT.request(req -> req.method(m).uri(fake)).get();
      // Assert
      Assertions.assertEquals(400, resp.statusCode());
    }
  }
}
