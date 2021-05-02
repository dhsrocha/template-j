package template.feature.address;

import io.javalin.plugin.openapi.annotations.HttpMethod;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
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
  private static final Address VALID_STUB = stub(1).findAny().orElseThrow();
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
      + "WHEN perform address retrieve operation "
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
                         .thenSerializeTo(Address.class);
    // Act
    val filtered = CLIENT.filter(criteria).thenMap();
    // Assert
    Assertions.assertEquals(1, filtered.size());
  }

  @Test
  @DisplayName(""
      + "GIVEN 30 address resources created"
      + "AND parameters to skip 5 and limit 15 resources "
      + "WHEN perform user retrieve operation "
      + "THEN return from the 6th resource to the 20th one.")
  final void given30Created_andParamsSkip5and10limit_whenRetrieving_thenReturn6thTo20th() {
    // Arrange
    stub(30)
        .map(a -> CLIENT.request(req -> req.method(HttpMethod.POST).body(a)))
        .map(Supplier::get)
        .forEachOrdered(r -> Assertions.assertEquals(201, r.statusCode()));
    // Act
    val found = CLIENT
        .request(req -> req.method(HttpMethod.GET).params(Map.of("limit", "15",
                                                                 "skip", "5")))
        .thenSerializeTo(Map.class);
    // Assert
    Assertions.assertEquals(15, found.size());
  }

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName(""
      + "GIVEN three created resources "
      + "WHEN perform address retrieve operation "
      + "THEN return all resources created.")
  final void given3createdResources_whenRetrieving_thenReturnAllResourcesCreated() {
    // Arrange
    val ids = stub(3)
        .map(a -> CLIENT.request(req -> req.method(HttpMethod.POST).body(a)))
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
      + "WHEN performing address creation "
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

  private static Stream<Address> stub(final int bound) {
    return IntStream
        .rangeClosed(1, bound)
        .mapToObj(i -> Address.builder()
                              .type(Address.Type.values()[i % Address.Type
                                  .values().length])
                              .place(String.valueOf(i))
                              .number(String.valueOf(i))
                              .neighbourhood(String.valueOf(i))
                              .municipality(String.valueOf(i))
                              .state(String.valueOf(i))
                              .postalCode(String.valueOf(i))
                              .build());
  }
}
