package template.feature.user;

import io.javalin.plugin.openapi.annotations.HttpMethod;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import template.Application.Feat;
import template.Client;
import template.Support.IntegrationTest;

@IntegrationTest(Feat.USER)
@DisplayName("User feature test suite using integration test strategy.")
final class UserTest {

  private static final Client<User> CLIENT = Client.create(User.class);
  private static final User VALID_STUB = stub(1).findAny().orElseThrow();
  private static final Map<String, String> INVALID_STUB = Map
      .of("age", "0", "name", "some");

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
        .thenSerializeTo(User.class);
    Assertions.assertEquals(VALID_STUB, found);
  }

  @Test
  @DisplayName(""
      + "GIVEN three distinct resources "
      + "AND a request body as filtering criterion "
      + "WHEN perform retrieve operation "
      + "THEN return resources with matching attributes from request body.")
  final void given3created_andFilteringCriterion_whenRetrieving_thenReturnMatching() {
    // Arrange
    val ids = stub(3)
        .map(u -> CLIENT.request(
            req -> req.method(HttpMethod.POST).body(u)))
        .map(req -> req.get().body())
        .toArray(String[]::new);
    val pick = ids[new Random().nextInt(ids.length)];
    val criteria = CLIENT.request(req -> req.method(HttpMethod.GET).uri(pick))
                         .thenSerializeTo(User.class);
    // Act
    val filtered = CLIENT.filter(criteria).thenMap();
    // Assert
    Assertions.assertEquals(1, filtered.size());
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName(""
      + "GIVEN three created resources "
      + "WHEN perform user retrieve operation "
      + "THEN return all resources created.")
  final void given3createdResources_whenRetrieving_thenReturnAllResourcesCreated() {
    // Arrange
    val ids = stub(3)
        .map(u -> CLIENT.request(req -> req.method(HttpMethod.POST).body(u)))
        .map(req -> req.thenSerializeTo(UUID.class))
        .sorted().toArray(UUID[]::new);
    // Act
    val found = (Map<String, ?>) CLIENT
        .request(req -> req.method(HttpMethod.GET)).thenSerializeTo(Map.class);
    // Assert
    val arr = found.keySet().stream().map(UUID::fromString).sorted()
                   .toArray(UUID[]::new);
    Assertions.assertArrayEquals(ids, arr);
  }

  @Test
  @DisplayName(""
      + "GIVEN a created resource "
      + "WHEN perform user update operation "
      + "THEN return true.")
  final void givenCreatedResource_whenUpdating_thenReturnTrue() {
    // Arrange
    val created = CLIENT
        .request(req -> req.method(HttpMethod.POST).body(VALID_STUB))
        .thenSerializeTo(UUID.class);
    val toUpdate = User.of("updated", 5);
    // Act
    val isUpdated = CLIENT.request(
        req -> req.method(HttpMethod.PATCH).uri(created).body(toUpdate)).get();
    // Assert
    Assertions.assertEquals(204, isUpdated.statusCode());
    val found = CLIENT.request(req -> req.method(HttpMethod.GET).uri(created))
                      .thenSerializeTo(User.class);
    Assertions.assertEquals(toUpdate, found);
  }

  @Test
  @DisplayName(""
      + "GIVEN a created resource "
      + "WHEN perform user delete operation "
      + "THEN return true.")
  final void givenCreatedResource_whenDeleting_thenReturnTrue() {
    // Arrange
    val created = CLIENT
        .request(req -> req.method(HttpMethod.POST).body(VALID_STUB))
        .thenSerializeTo(UUID.class);
    // Act
    val isDeleted = CLIENT
        .request(req -> req.method(HttpMethod.DELETE).uri(created)).get();
    // Assert
    Assertions.assertEquals(204, isDeleted.statusCode());
    val notFound = CLIENT
        .request(req -> req.method(HttpMethod.GET).uri(created)).get();
    Assertions.assertEquals(404, notFound.statusCode());
  }

  @Test
  @DisplayName(""
      + "GIVEN invalid request "
      + "WHEN performing user creation operation "
      + "THEN return 422 as HTTP status code.")
  final void givenInvalidRequest_whenCreating_thenReturn422asStatus() {
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
  final void givenInvalidFilterQuery_whenRetrieving_thenReturn400asStatus() {
    // Arrange
    val params = Map.of("fq", "xp");
    // Act
    val resp = CLIENT
        .request(req -> req.method(HttpMethod.GET).params(params)).get();
    // Assert
    Assertions.assertEquals(400, resp.statusCode());
  }

  private static Stream<User> stub(final int bound) {
    return IntStream.rangeClosed(1, bound)
                    .mapToObj(i -> User.of(String.valueOf(i), i));
  }
}
