package template.feature.address;

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
import template.feature.address.Address.AddressBuilder;

@IntegrationTest(Feat.ADDRESS)
@DisplayName("Address feature test suite using integration test strategy.")
final class AddressTest {

  private static final Client<Address> CLIENT = Client.create(Address.class);
  private static final Address VALID_STUB = Address.builder()
                                                   .type(Address.Type.ROAD)
                                                   .place("a")
                                                   .number("a")
                                                   .neighbourhood("a")
                                                   .municipality("a")
                                                   .state("a")
                                                   .postalCode("a")
                                                   .build();
  private static final Map<String, String> INVALID_STUB =
      Map.of("type", "",
             "place", "",
             "number", "",
             "neighbourhood", "",
             "municipality", "",
             "state", "",
             "postalCode", "");

  @Test
  @DisplayName(""
      + "GIVEN valid resource to persist "
      + "WHEN performing address create operation "
      + "THEN should be able to find resource.")
  final void givenValidResource_whenCreating_thenShouldAbleToFindResource() {
    // Arrange
    val body = Client.jsonOf(VALID_STUB);
    // Act
    val resp = CLIENT.perform(HttpRequest.newBuilder().POST(body));
    // Assert
    Assertions.assertEquals(201, resp.statusCode());
    val found = CLIENT.perform(Address.class,
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
    val ids = IntStream.rangeClosed(0, 2)
                       .mapToObj(i -> Address.builder()
                                             .type(Address.Type.values()[i])
                                             .place(String.valueOf(i))
                                             .number(String.valueOf(i))
                                             .neighbourhood(String.valueOf(i))
                                             .municipality(String.valueOf(i))
                                             .state(String.valueOf(i))
                                             .postalCode(String.valueOf(i)))
                       .map(AddressBuilder::build)
                       .map(Client::jsonOf)
                       .map(HttpRequest.newBuilder()::POST)
                       .map(req -> CLIENT.perform(UUID.class, req))
                       .toArray(UUID[]::new);
    val pick = ids[new Random().nextInt(ids.length)];
    val criteria = CLIENT
        .perform(Address.class, pick, HttpRequest.newBuilder().GET());
    // Act
    val filtered = (Map<String, ?>) CLIENT.getWith(criteria, Map.class);
    // Assert
    Assertions.assertEquals(1, filtered.size());
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName(""
      + "GIVEN three created resources "
      + "WHEN perform address retrieve operation "
      + "THEN return all resources created.")
  final void given3createdResources_whenRetrieving_thenReturnAllResourcesCreated() {
    // Arrange
    val ids = IntStream.rangeClosed(1, 3).mapToObj(String::valueOf)
                       .map(VALID_STUB.toBuilder()::number)
                       .map(AddressBuilder::build)
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
      + "WHEN performing address update operation "
      + "THEN return true.")
  final void givenCreatedResource_whenUpdating_thenReturnTrue() {
    // Arrange
    val created = CLIENT.perform(
        UUID.class, HttpRequest.newBuilder().POST(Client.jsonOf(VALID_STUB)));
    val toUpdate = VALID_STUB.toBuilder()
                             .place("otherPlace")
                             .number("otherNumber")
                             .build();
    val body = Client.jsonOf(toUpdate);
    val req = HttpRequest.newBuilder().method(HttpMethod.PATCH.name(), body);
    // Act
    val isUpdated = CLIENT.perform(created, req);
    // Assert
    Assertions.assertEquals(204, isUpdated.statusCode());
    val found = CLIENT
        .perform(Address.class, created, HttpRequest.newBuilder().GET());
    Assertions.assertEquals(toUpdate, found);
  }

  @Test
  @DisplayName(""
      + "GIVEN a created resource "
      + "WHEN performing address delete operation "
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
      + "WHEN performing address resource creation "
      + "THEN return 422 as HTTP status code.")
  final void givenInvalidRequest_whenCreating_thenReturn422asStatus() {
    // Arrange
    val body = Client.jsonOf(INVALID_STUB);
    // Act
    val resp = CLIENT.perform(HttpRequest.newBuilder().POST(body));
    // Assert
    Assertions.assertEquals(422, resp.statusCode());
  }
}
