package template;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.Serializable;
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
import java.util.UUID;
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
  class Client<T> {

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
    public static <T> Client<T> create() {
      return new Client<>(base());
    }

    /**
     * Instantiates an HTTP client with base URL listening to the same port the
     * server started instance is binding to and also pointing out to the
     * provided {@link Domain}'s expected endpoint.
     *
     * @param ref Domain class to prefix base .
     * @return Instance with the adapted base URL.
     */
    public static <D extends Domain<D>> Client<D> create(
        final @lombok.NonNull Class<D> ref) {
      return new Client<>(base(ref.getSimpleName().toLowerCase()));
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

    @lombok.SneakyThrows
    public HttpResponse<String> perform(
        final @lombok.NonNull HttpRequest.Builder build) {
      return CLIENT.send(build.uri(base).build(), BodyHandlers.ofString());
    }

    @lombok.SneakyThrows
    public HttpResponse<String> perform(final @lombok.NonNull UUID id,
                                        final @lombok.NonNull HttpRequest.Builder build) {
      return CLIENT.send(build.uri(URI.create(base + "/" + id))
                              .build(), BodyHandlers.ofString());
    }

    public <U> U perform(final @lombok.NonNull Class<U> serializeTo,
                         final @lombok.NonNull HttpRequest.Builder build) {
      return serialize(perform(build).body(), serializeTo);
    }

    public <U> U perform(final @lombok.NonNull Class<U> serializeTo,
                         final @lombok.NonNull UUID id,
                         final @lombok.NonNull HttpRequest.Builder build) {
      return serialize(perform(id, build).body(), serializeTo);
    }

    private static <U> U serialize(final @lombok.NonNull String source,
                                   final @lombok.NonNull Class<U> serializeTo) {
      val t = MAPPER.fromJson(source, serializeTo);
      Exceptions.ILLEGAL_ARGUMENT.throwIf(() -> null == t);
      return t;
    }

    @lombok.SneakyThrows
    private static URI base(final Serializable... params) {
      val host = InetAddress.getLocalHost().getHostAddress();
      val pp = Arrays.stream(params)
                     .map(String::valueOf).collect(Collectors.joining("/"));
      return URI.create("http://" + host + ":" + PORT + "/" + pp);
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
      REF.set(Bootstrap.bootstrap(
          Props.MODE.is(Mode.TEST),
          Props.PORT.is(Client.PORT),
          Props.FEAT.is(Arrays.toString(feats).replaceAll("[\\[\\] ]", ""))));
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
