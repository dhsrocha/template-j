package template.feature.info;

import java.net.http.HttpRequest;
import java.util.Arrays;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import template.Application.Feat;
import template.Support.Client;
import template.Support.IntegrationTest;
import template.core.Props;

/**
 * Test suite for application information endpoint.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@IntegrationTest
@DisplayName("Test suite for application information endpoint.")
class InfoTest {

  /**
   * WHEN retrieve from default exposed endpoint
   * THEN return HTTP 200 as status
   * AND application information object as body."
   */
  @Test
  @DisplayName(""
      + "WHEN retrieve from default exposed endpoint "
      + "THEN return HTTP 200 as status "
      + "AND application information object as body.")
  final void whenDefaultEndpoint_thenRespond200status_andAppObjInfoAsBody() {
    // Act
    val info = Client.create().perform(Info.class,
                                       HttpRequest.newBuilder().GET());
    // Assert
    val map = Map.of(Props.FEAT.name(), Arrays.toString(Feat.values()));
    Assertions.assertEquals(Info.of(map), info);
  }
}
