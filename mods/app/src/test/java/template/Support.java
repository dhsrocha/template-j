package template;

import com.google.gson.Gson;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.val;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import template.Application.Feat;
import template.Application.Mode;
import template.Web.Server;
import template.base.Exceptions;
import template.base.stereotype.Domain;

/**
 * Supporting assets for testing purposes.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Support {

  /**
   * Activates {@link Bootstrap#bootstrap(String...)} to test endpoints under
   * <b>integration testing strategy</b>. Must be used along with
   * {@link Client}.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @ExtendWith({AppExtension.class})
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @interface IntegrationTest {
    /**
     * Features to activate on application startup. Default value means
     * loading up {@link Feat#values() all features available}. Values added
     * to {@link #activated()} method have higher topmost priority.
     *
     * @return Feature entries.
     * @see #activated()
     */
    Feat[] value() default {};

    /**
     * Features to activate on application startup. Default value means
     * loading up {@link Feat#values() all features available}. Values added
     * here have higher topmost priority.
     *
     * @return Feature entries.
     */
    Feat[] activated() default {};
  }

  /**
   * Performs requests to the instance started up by {@link IntegrationTest}.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @lombok.Value
  class Client {

    private static final Gson MAPPER = new Gson();
    private static final int PORT = nextAvailablePort();
    private static final HttpClient CLIENT = HttpClient
        .newBuilder().version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(1)).build();

    URI base;

    /**
     * Instantiates an HTTP client with base URL listening to the same port the
     * server started instance is binding to.
     *
     * @return Instance with the adapted base URL.
     */
    public static Client create() {
      return new Client(base());
    }

    /**
     * Instantiates an HTTP client with base URL listening to the same port the
     * server started instance is binding to and also pointing out to the
     * provided {@link Domain}'s expected endpoint.
     *
     * @param domain Domain class to prefix base .
     * @return Instance with the adapted base URL.
     */
    public static Client create(
        final @lombok.NonNull Class<? extends Domain<?>> domain) {
      return new Client(base("/" + domain.getSimpleName().toLowerCase()));
    }

    /**
     * Support method to construct json body for a sending request.
     *
     * @param toBody an object as request body.
     * @return {@link BodyPublisher}'s instance.
     */
    public static BodyPublisher jsonOf(final @NonNull Object toBody) {
      return BodyPublishers.ofString(MAPPER.toJson(toBody));
    }

    public HttpResponse<String> perform(
        final @lombok.NonNull HttpRequest.Builder build) {
      return perform(URI.create("/"), build);
    }

    @lombok.SneakyThrows
    public HttpResponse<String> perform(
        final @lombok.NonNull HttpRequest.Builder build,
        final @lombok.NonNull String... extensions) {
      val u = URI.create(base.toString() + "/" + String.join("/", extensions));
      return CLIENT.send(build.uri(u).build(), BodyHandlers.ofString());
    }

    public <T> T perform(final @lombok.NonNull Class<T> serializeTo,
                         final @lombok.NonNull HttpRequest.Builder build) {
      return perform(URI.create("/"), serializeTo, build);
    }

    public <T> T perform(final @lombok.NonNull Class<T> domain,
                         final @lombok.NonNull HttpRequest.Builder build,
                         final @lombok.NonNull String... extensions) {
      val t = MAPPER.fromJson(perform(build, extensions).body(), domain);
      Exceptions.ILLEGAL_ARGUMENT.throwIf(() -> null == t);
      return t;
    }

    @lombok.SneakyThrows
    public HttpResponse<String> perform(final @lombok.NonNull URI uri,
                                        final @lombok.NonNull HttpRequest.Builder build) {
      return CLIENT.send(build.uri(base.resolve(uri)).build(),
                         HttpResponse.BodyHandlers.ofString());
    }

    public <T> T perform(final @lombok.NonNull URI uri,
                         final @lombok.NonNull Class<T> serializeTo,
                         final @lombok.NonNull HttpRequest.Builder build) {
      return MAPPER.fromJson(perform(uri, build).body(), serializeTo);
    }

    @lombok.SneakyThrows
    private static URI base(final String... prefix) {
      val host = InetAddress.getLocalHost().getHostAddress();
      return URI.create("http://" + host + ":" + PORT
                            + String.join("/", prefix));
    }
  }

  /**
   * JUnit extension to start up {@link Bootstrap application's bootstrap}.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  final class AppExtension implements BeforeTestExecutionCallback,
                                      AfterTestExecutionCallback {
    private static final AtomicReference<Server> REF =
        new AtomicReference<>();

    @Override
    public void beforeTestExecution(final ExtensionContext ctx) {
      // Setup
      val suite = ctx.getTestInstance().orElseThrow().getClass()
                     .getAnnotation(IntegrationTest.class);
      val feats = Optional.of(suite.activated())
                          .or(() -> Optional.of(suite.value()))
                          .filter(l -> l.length > 0)
                          .orElseGet(Feat::values);
      val str = Arrays.stream(feats).map(Enum::name)
                      .collect(Collectors.joining(","));
      REF.set(Bootstrap.bootstrap(
          Props.MODE.is(Mode.TEST),
          Props.PORT.is(Client.PORT),
          Props.FEAT.is(str)));
    }

    @Override
    public void afterTestExecution(final ExtensionContext ctx) {
      REF.get().stop();
    }
  }

  /**
   * Recursively loops until finding a available TCP port, locks it, get its
   * value and then releases it.
   *
   * @return next available port.
   */
  private static int nextAvailablePort() {
    try (val s = new ServerSocket(0)) {
      return s.getLocalPort();
    } catch (final IOException e) {
      return nextAvailablePort();
    }
  }
}
