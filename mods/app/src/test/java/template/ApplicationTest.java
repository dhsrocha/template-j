package template;

import java.net.http.HttpRequest;
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import template.Application.Feat;
import template.Support.Client;
import template.Support.IntegrationTest;

/**
 * Application's test case.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@IntegrationTest
@DisplayName("Application's smoke test cases.")
final class ApplicationTest {

  /**
   * WHEN retrieve from default exposed endpoint
   * THEN return HTTP 200 as status
   * AND "[{@link Props#values()}] as body."
   */
  @Test
  @DisplayName(""
      + "WHEN retrieve from default exposed endpoint "
      + "THEN return HTTP 200 as status "
      + "AND Props.values()'s as body.")
  final void whenDefaultEndpoint_thenRespond200status_andPropsValuesAsBody() {
    // Act
    val res = Client.create().perform(HttpRequest.newBuilder().GET());
    // Assert
    Assertions.assertEquals(Arrays.toString(Feat.values()), res.body());
  }
}
