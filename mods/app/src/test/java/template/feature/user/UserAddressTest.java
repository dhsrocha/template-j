package template.feature.user;

import static template.feature.StubSupport.addressStub;
import static template.feature.StubSupport.userStub;

import io.javalin.plugin.openapi.annotations.HttpMethod;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import template.Application.Feat;
import template.Client;
import template.Support.IntegrationTest;
import template.feature.address.Address;

/**
 * Suite for evaluating {@link Address} feature's endpoints aggregated with
 * {@link User}'s.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("ClassCanBeStatic")
@DisplayName("Suite for evaluating address' endpoints aggregated with user's.")
final class UserAddressTest {

  private static final Client<User> USER = Client.create(User.class);
  private static final Client<Address> ADDRESS = Client.create(Address.class);
  private static final Map<String, String> INVALID_STUB =
      Map.of("type", "",
             "place", "",
             "number", "",
             "neighbourhood", "",
             "municipality", "",
             "state", "",
             "postalCode", "");

  @Nested
  @IntegrationTest({Feat.USER, Feat.ADDRESS})
  @DisplayName("Operations for linking and unlinking two resources.")
  final class Linking {

    @ParameterizedTest
    @CsvSource({"PATCH, 200", "DELETE, 404"})
    @DisplayName(""
        + "GIVEN an user and an address resources created "
        + "WHEN perform link or unlink operation between them "
        + "AND try to perform same previous operation "
        + "AND try to to find resource supposedly bound "
        + "THEN return 204 as status code "
        + "AND return 409 as status code "
        + "AND verify if (un)binding is done.")
    final void givenUserAddress_andLinked_whenLinkTwice_thenReturn204_andThen422asStatus(
        final String httpMethod, final int isLinkedStatus) {
      // Arrange
      val stub = StubPair.create();
      val cli = USER.compose(stub.user, Address.class);
      val method = HttpMethod.valueOf(httpMethod);
      if (HttpMethod.DELETE == method) {
        val previous = cli.request(req -> req.method(HttpMethod.PATCH)
                                             .uri(stub.address)).get();
        Assertions.assertEquals(204, previous.statusCode());
      }
      // Act
      val success = cli.request(req -> req.method(method)
                                          .uri(stub.address)).get();
      val failLink = cli.request(req -> req.method(method)
                                           .uri(stub.address)).get();
      val tryFind = cli.request(req -> req.method(HttpMethod.GET)
                                          .uri(stub.address)).get();
      // Assert
      Assertions.assertEquals(204, success.statusCode());
      Assertions.assertEquals("", success.body());
      Assertions.assertEquals(409, failLink.statusCode());
      Assertions.assertFalse(failLink.body().isBlank());
      Assertions.assertEquals(isLinkedStatus, tryFind.statusCode());
    }
  }

  @Nested
  @IntegrationTest({Feat.USER, Feat.ADDRESS})
  @DisplayName("Create resources and link them to provided user resource.")
  final class CreateOn {

    @Test
    @DisplayName(""
        + "GIVEN an user and an address resources created "
        + "WHEN perform create operation "
        + "THEN return HTTP 201 as status code "
        + "AND address resource linked to provided user identity.")
    final void givenUserAddress_whenCreate_thenReturn201asStatus_andLinked() {
      // Arrange
      val stub = StubPair.create();
      val cli = USER.compose(stub.user, Address.class);
      // Act
      val resp = cli
          .request(req -> req.method(HttpMethod.POST).body(addressStub(1)
                                                               .toArray()[0]));
      // Assert
      val created = resp.thenTurnInto(UUID.class);
      Assertions.assertEquals(201, resp.get().statusCode());
      val failed = cli.request(req -> req.method(HttpMethod.PATCH)
                                         .uri(created)).get();
      Assertions.assertEquals(409, failed.statusCode());
    }

    @Test
    @DisplayName(""
        + "GIVEN an user created "
        + "AND an invalid address to create "
        + "WHEN perform create operation "
        + "THEN return HTTP 422 as status code.")
    final void givenUserCreated_andInvalidAddressToCreate_whenCreate_thenReturn422asStatus() {
      // Arrange
      val stub = StubPair.create();
      val cli = USER.compose(stub.user, Address.class);
      // Act
      val failed = cli
          .request(r -> r.method(HttpMethod.POST).body(INVALID_STUB));
      // Assert
      Assertions.assertEquals(422, failed.get().statusCode());
    }
  }

  @Nested
  @IntegrationTest({Feat.USER, Feat.ADDRESS})
  @DisplayName("Retrieve address based on an existent user resource.")
  final class RetrieveOn {

    @ParameterizedTest
    @CsvSource({"skip, 1, 2", "limit, 1, 1"})
    @DisplayName(""
        + "GIVEN 1 user and 3 linked address resources created "
        + "AND request with parameters to skip and limit resources "
        + "WHEN perform user retrieve operation "
        + "THEN return filtered accordingly.")
    final void givenSomeCreated_andSomeLinked_andSkipLimit_whenRetrieve_thenReturnAccordingly(
        final String key, final String value, final int expected) {
      // Arrange
      val user = userStub(1)
          .map(u -> USER.request(req -> req.method(HttpMethod.POST).body(u)))
          .map(r -> r.thenTurnInto(UUID.class))
          .toArray(UUID[]::new)[0];
      val cli = USER.compose(user, Address.class);
      addressStub(3)
          .map(a -> ADDRESS.request(req -> req.method(HttpMethod.POST).body(a)))
          .map(r -> r.thenTurnInto(UUID.class))
          .map(id -> cli.request(r -> r.method(HttpMethod.PATCH).uri(id)).get())
          .forEach(b -> Assertions.assertEquals(204, b.statusCode()));
      val params = Map.of(key, value);
      // Act
      val filtered = cli.retrieve(params).thenMap();
      // Assert
      Assertions.assertEquals(expected, filtered.size());
    }

    @Test
    @DisplayName(""
        + "GIVEN 1 user and 4 address resources created "
        + "AND 2 address resources linked to the user one "
        + "AND a request body as filtering criterion "
        + "WHEN perform user retrieve operation "
        + "THEN return the linked and filtered one.")
    final void givenUser4Addresses_and2Linked_whenRetrieve_thenReturnLinkedFiltered() {
      // Arrange
      val stub = StubPair.create();
      val address = addressStub(3)
          .map(a -> ADDRESS.request(req -> req.method(HttpMethod.POST).body(a)))
          .map(r -> r.thenTurnInto(UUID.class))
          .toArray(UUID[]::new);
      val cli = USER.compose(stub.user, Address.class);
      Stream.of(address[0], address[1]).forEach(id -> {
        val resp = cli.request(r -> r.method(HttpMethod.PATCH).uri(id));
        Assertions.assertEquals(204, resp.get().statusCode());
      });
      val criteria = cli
          .request(req -> req.method(HttpMethod.GET).uri(address[0]))
          .thenTurnInto(Address.class);
      // Act
      val filtered = cli.retrieve(criteria).thenMap();
      // Assert
      Assertions.assertTrue(filtered.containsKey(address[0])); // Wanted
      Assertions.assertFalse(filtered.containsKey(address[1])); // Linked only
      Assertions.assertFalse(filtered.containsKey(address[2])); // Unlinked
      Assertions.assertFalse(filtered.containsKey(stub.address)); // Unlinked
    }

    @Test
    @DisplayName(""
        + "GIVEN an user and an address resources created "
        + "WHEN perform operation to retrieve address composed from user "
        + "THEN return HTTP 404 as status code.")
    final void givenSomeCreated_whenRetrieve_thenReturn404asStatus() {
      // Arrange
      val stub = StubPair.create();
      val cli = USER.compose(stub.user, Address.class);
      // Act
      val notFound = cli.request(req -> req.method(HttpMethod.GET)
                                           .uri(stub.address)).get();
      // Assert
      Assertions.assertEquals(404, notFound.statusCode());
    }
  }

  /**
   * Supporting class to create data mass required for test methods work in a
   * standard way.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @Getter(AccessLevel.NONE)
  @Value(staticConstructor = "create")
  private static class StubPair {
    UUID user = userStub(1)
        .map(u -> USER.request(req -> req.method(HttpMethod.POST).body(u)))
        .map(r -> r.thenTurnInto(UUID.class))
        .toArray(UUID[]::new)[0];
    UUID address = addressStub(1)
        .map(a -> ADDRESS.request(req -> req.method(HttpMethod.POST).body(a)))
        .map(r -> r.thenTurnInto(UUID.class))
        .toArray(UUID[]::new)[0];
  }
}
