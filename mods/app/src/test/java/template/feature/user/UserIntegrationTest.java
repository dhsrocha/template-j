package template.feature.user;

import io.javalin.plugin.openapi.annotations.HttpMethod;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import template.Application.Feat;
import template.Support.Client;
import template.Support.IntegrationTest;

@IntegrationTest(Feat.USER)
@DisplayName("User feature test suite using integration test strategy.")
final class UserIntegrationTest {

  private static final Client CLIENT = Client.create(User.class);
  private static final User VALID_STUB = User.of("user", 1);

  @Test
  @DisplayName(""
      + "GIVEN valid resource to persist "
      + "WHEN performing user created operation "
      + "THEN should be able to find resource.")
  final void givenValidResource_whenCreating_thenShouldAbleToFindResource() {
    // Arrange
    val body = Client.jsonOf(VALID_STUB);
    // Act
    val resp = CLIENT.perform(HttpRequest.newBuilder().POST(body));
    // Assert
    Assertions.assertEquals(201, resp.statusCode());
    val found = CLIENT
        .perform(User.class, HttpRequest.newBuilder().GET(), resp.body());
    Assertions.assertEquals(VALID_STUB, found);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName(""
      + "GIVEN three created resources "
      + "WHEN perform retrieve operation "
      + "THEN return all resources created.")
  final void given3createdResources_whenRetrieving_thenReturnAllResourcesCreated() {
    // Arrange
    val ids = IntStream.rangeClosed(1, 3)
                       .mapToObj(i -> User.of(String.valueOf(i), i))
                       .map(Client::jsonOf)
                       .map(HttpRequest.newBuilder()::POST)
                       .map(req -> CLIENT.perform(UUID.class, req))
                       .sorted().toArray(UUID[]::new);
    // Act
    val found = (Map<String, ?>) CLIENT
        .perform(Map.class, HttpRequest.newBuilder().GET());
    // Assert
    val arr = found.keySet().stream().map(UUID::fromString).sorted()
                   .toArray(UUID[]::new);
    Assertions.assertArrayEquals(ids, arr);
  }

  @Test
  @DisplayName(""
      + "GIVEN a created resource "
      + "WHEN performing user update operation "
      + "THEN return true.")
  final void givenCreatedResource_whenUpdating_thenReturnTrue() {
    // Arrange
    val created = CLIENT.perform(
        UUID.class, HttpRequest.newBuilder().POST(Client.jsonOf(VALID_STUB)));
    val toUpdate = User.of("updated", 5);
    val body = Client.jsonOf(toUpdate);
    // Act
    val isUpdated = CLIENT.perform(
        HttpRequest.newBuilder().method(HttpMethod.PATCH.name(), body),
        created);
    // Assert
    Assertions.assertEquals(204, isUpdated.statusCode());
    val found = CLIENT
        .perform(User.class, HttpRequest.newBuilder().GET(), created);
    Assertions.assertEquals(toUpdate, found);
  }

  @Test
  @DisplayName(""
      + "GIVEN a created resource "
      + "WHEN performing user delete operation "
      + "THEN return true.")
  final void givenCreatedResource_whenDeleting_thenReturnTrue() {
    // Arrange
    val body = Client.jsonOf(VALID_STUB);
    val created = CLIENT
        .perform(UUID.class, HttpRequest.newBuilder().POST(body));
    // Act
    val isUpdated = CLIENT.perform(HttpRequest.newBuilder().DELETE(), created);
    // Assert
    Assertions.assertEquals(204, isUpdated.statusCode());
    val resp = CLIENT
        .perform(HttpRequest.newBuilder().GET(), created);
    Assertions.assertEquals(404, resp.statusCode());
  }

  @Test
  @DisplayName(""
      + "GIVEN invalid request "
      + "WHEN performing user resource creation "
      + "THEN return 422 as HTTP status code.")
  final void givenInvalidRequest_whenCreating_thenReturn422asStatus() {
    // Arrange
    val body = Client.jsonOf(Map.of("age", "0", "name", "some"));
    // Act
    val resp = CLIENT.perform(HttpRequest.newBuilder().POST(body));
    // Assert
    Assertions.assertEquals(422, resp.statusCode());
  }
}
