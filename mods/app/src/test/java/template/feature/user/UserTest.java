package template.feature.user;

import io.javalin.plugin.openapi.annotations.HttpMethod;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.Random;
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
final class UserTest {

  private static final Client<User> CLIENT = Client.create(User.class);
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
    val found = CLIENT.perform(User.class,
                               UUID.fromString(resp.body()),
                               HttpRequest.newBuilder().GET());
    Assertions.assertEquals(VALID_STUB, found);
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName(""
      + "GIVEN three distinct resources "
      + "AND a request body as filtering criterion "
      + "WHEN perform retrieve operation "
      + "THEN return resources with matching attributes from request body.")
  final void given3created_andFilteringCriterion_whenRetrieving_thenReturnMatching() {
    // Arrange
    val ids = IntStream.rangeClosed(1, 3)
                       .mapToObj(i -> User.of(String.valueOf(i), i))
                       .map(Client::jsonOf)
                       .map(HttpRequest.newBuilder()::POST)
                       .map(req -> CLIENT.perform(UUID.class, req))
                       .toArray(UUID[]::new);
    val pick = ids[new Random().nextInt(ids.length)];
    val criteria = CLIENT
        .perform(User.class, pick, HttpRequest.newBuilder().GET());
    // Act
    val filtered = (Map<String, ?>) CLIENT.getWith(criteria, Map.class);
    // Assert
    Assertions.assertEquals(1, filtered.size());
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
    val req = HttpRequest.newBuilder().method(HttpMethod.PATCH.name(), body);
    // Act
    val isUpdated = CLIENT.perform(created, req);
    // Assert
    Assertions.assertEquals(204, isUpdated.statusCode());
    val found = CLIENT
        .perform(User.class, created, HttpRequest.newBuilder().GET());
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
    val isUpdated = CLIENT.perform(created, HttpRequest.newBuilder().DELETE());
    // Assert
    Assertions.assertEquals(204, isUpdated.statusCode());
    val resp = CLIENT.perform(created, HttpRequest.newBuilder().GET());
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
