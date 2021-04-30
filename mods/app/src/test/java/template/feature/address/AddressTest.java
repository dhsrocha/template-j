package template.feature.address;

import io.javalin.plugin.openapi.annotations.HttpMethod;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import template.Application.Feat;
import template.Client;
import template.Support.IntegrationTest;

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
    // Act
    val resp = CLIENT
        .request(req -> req.method(HttpMethod.POST).body(VALID_STUB)).get();
    // Assert
    Assertions.assertEquals(201, resp.statusCode());
    val found = CLIENT
        .request(req -> req.method(HttpMethod.GET).uri(resp.body()))
        .thenSerializeTo(Address.class);
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
    val ids = IntStream.rangeClosed(0, 2)
                       .mapToObj(i -> Address.builder()
                                             .type(Address.Type.values()[i])
                                             .place(String.valueOf(i))
                                             .number(String.valueOf(i))
                                             .neighbourhood(String.valueOf(i))
                                             .municipality(String.valueOf(i))
                                             .state(String.valueOf(i))
                                             .postalCode(String.valueOf(i))
                                             .build())
                       .map(u -> CLIENT.request(
                           req -> req.method(HttpMethod.POST).body(u)))
                       .map(req -> req.get().body())
                       .toArray(String[]::new);
    val pick = ids[new Random().nextInt(ids.length)];
    val criteria = CLIENT.request(req -> req.method(HttpMethod.GET).uri(pick))
                         .thenSerializeTo(Address.class);
    // Act
    val filtered = CLIENT.filter(criteria).thenMap();
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
                       .mapToObj(String::valueOf)
                       .map(VALID_STUB.toBuilder()::number)
                       .map(Address.AddressBuilder::build)
                       .map(a -> CLIENT.request(
                           req -> req.method(HttpMethod.POST).body(a)))
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
      + "WHEN performing address update operation "
      + "THEN return true.")
  final void givenCreatedResource_whenUpdating_thenReturnTrue() {
    // Arrange
    val created = CLIENT
        .request(req -> req.method(HttpMethod.POST).body(VALID_STUB))
        .thenSerializeTo(UUID.class);
    val toUpdate = VALID_STUB.toBuilder()
                             .place("otherPlace")
                             .number("otherNumber")
                             .build();
    // Act
    val isUpdated = CLIENT.request(
        req -> req.method(HttpMethod.PATCH).uri(created).body(toUpdate)).get();
    // Assert
    Assertions.assertEquals(204, isUpdated.statusCode());
    val found = CLIENT.request(req -> req.method(HttpMethod.GET).uri(created))
                      .thenSerializeTo(Address.class);
    Assertions.assertEquals(toUpdate, found);
  }

  @Test
  @DisplayName(""
      + "GIVEN a created resource "
      + "WHEN performing address delete operation "
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
      + "WHEN performing address resource creation "
      + "THEN return 422 as HTTP status code.")
  final void givenInvalidRequest_whenCreating_thenReturn422asStatus() {
    // Act
    val resp = CLIENT
        .request(req -> req.method(HttpMethod.POST).body(INVALID_STUB)).get();
    // Assert
    Assertions.assertEquals(422, resp.statusCode());
  }
}
