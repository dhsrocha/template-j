package template.feature;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import template.base.stereotype.Domain;
import template.feature.address.Address;
import template.feature.user.User;

/**
 * Supporting class to provide {@link Domain}-related stubs objects.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StubSupport {

  /**
   * Produces {@link User} instances for stubbing tests.
   *
   * @param bound Amount of stub instances to produce.
   * @return Stream of distinct {@link User} produced instances.
   */
  public static Stream<User> userStub(final int bound) {
    return IntStream.rangeClosed(1, bound)
                    .mapToObj(i -> User.of(String.valueOf(i),
                                           i + "@" + i + ".com",
                                           String.valueOf(i),
                                           i));
  }

  /**
   * Produces {@link Address} instances for stubbing tests.
   *
   * @param bound Amount of stub instances to produce.
   * @return Stream of distinct {@link Address} produced instances.
   */
  public static Stream<Address> addressStub(final int bound) {
    final var arr = Address.Type.values();
    return IntStream.range(0, bound)
                    .mapToObj(i -> Address.builder()
                                          .type(arr[i % arr.length])
                                          .place(String.valueOf(i))
                                          .number(String.valueOf(i))
                                          .neighbourhood(String.valueOf(i))
                                          .municipality(String.valueOf(i))
                                          .state(String.valueOf(i))
                                          .postalCode(String.valueOf(i))
                                          .build());
  }
}
