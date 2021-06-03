package template;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.val;
import org.eclipse.jetty.http.MimeTypes;
import template.Support.IntegrationTest;
import template.base.Exceptions;
import template.base.stereotype.Domain;

/**
 * Performs requests to the application's instance started up by
 * {@link IntegrationTest}.
 *
 * @param <T> Inferred type to drive which the instance should request from.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Client<T> {

  /**
   * Instantiates an HTTP client with base URL listening to the same port the
   * server started instance is binding to.
   *
   * @return Instance with the adapted base URL.
   */
  static Client<?> create() {
    return new Impl<>(base(), Request.builder());
  }

  /**
   * Instantiates an HTTP client with base URL listening to the same port the
   * server started instance is binding to and also pointing out to the provided
   * {@link Domain}'s expected endpoint.
   *
   * @param <D> Inferred type to drive which the instance should request from.
   * @param ref {@link Domain} type to be used conventionally as the feature
   *            routing.
   * @return Instance with the adapted base URL.
   */
  static <D extends Domain<D>> Client<D> create(
      final @lombok.NonNull Class<D> ref) {
    return new Impl<>(base(ref.getSimpleName().toLowerCase()),
                      Request.builder());
  }

  /**
   * Creates another client by appending URI based on provided parameters.
   *
   * @param id  Identity parameter related to provided domain context.
   * @param ref Domain context reference to append to base URI.
   * @param <D> Domain context type for aggregating.
   * @return Client with updated base URI.
   */
  <D extends Domain<D>> Client<D> compose(final @lombok.NonNull UUID id,
                                          final @lombok.NonNull Class<D> ref);

  /**
   * Prepares a request to be triggered afterwards.
   *
   * @param req {@link Request} configuration.
   * @return Client instance with a prompted request.
   */
  ThenSerialize request(
      final @lombok.NonNull UnaryOperator<Request.RequestBuilder> req);

  /**
   * Filters up a domain-based GET endpoint based on its attributes.
   *
   * @param fq Filtering query, based on domain's attributes to be used as
   *           matching criteria.
   * @return Client instance with a prompted request.
   * @see #retrieve(Object, Map)
   */
  default ThenFilter<T> retrieve(final @lombok.NonNull T fq) {
    return retrieve(fq, new HashMap<>());
  }

  /**
   * Filters up a domain-based GET endpoint based on its attributes.
   *
   * @return Client instance with a prompted request.
   * @see #retrieve(Object, Map)
   */
  default ThenFilter<T> retrieve() {
    return retrieve(new HashMap<>());
  }

  /**
   * Filters up a domain-based GET endpoint based on its attributes.
   *
   * @param params Request parameters.
   * @return Client instance with a prompted request.
   */
  ThenFilter<T> retrieve(final @lombok.NonNull Map<String, String> params);

  /**
   * Filters up a domain-based GET endpoint based on its attributes.
   *
   * @param fq     Filtering query, based on domain's attributes to be used as
   *               matching criteria.
   * @param params Non-conventional parameters to be attached to the {@code
   *               Request} URI.
   * @return Client instance with a prompted request.
   */
  ThenFilter<T> retrieve(final @lombok.NonNull T fq,
                         final @lombok.NonNull Map<String, String> params);

  /**
   * Step for serializing operations an a client with a prompted request.
   */
  interface ThenSerialize extends Supplier<HttpResponse<String>> {

    /**
     * Post step for serializing content coming from HTTP response.
     *
     * @param ref Type reference to be used on serialization.
     * @param <R> A type to serialize to.
     * @return The serialized instance.
     */
    <R> R thenTurnInto(final @lombok.NonNull Class<R> ref);
  }

  /**
   * Step for filtering operations an a client with a prompted request.
   *
   * @param <T> Inferred type to drive which the instance should request from.
   */
  interface ThenFilter<T> extends Supplier<HttpResponse<String>> {

    /**
     * Post step for serializing content coming from HTTP response under
     * the settled typing standard for domain-based endpoints.
     *
     * @return The typed map which maps returning resources indexed by the
     *     corresponding identity.
     */
    Map<UUID, T> thenMap();
  }

  /**
   * Build a base URI to be used on {@link Client}'s instantiation.
   *
   * @param segments segment components.
   * @return An URI with localhost joined with provided segment components.
   */
  @lombok.SneakyThrows
  private static URI base(final Serializable... segments) {
    val host = InetAddress.getLocalHost().getHostAddress() + ":" + Support.PORT;
    return URI.create("http://" + host + "/" + Arrays
        .stream(segments).map(String::valueOf)
        .collect(Collectors.joining("/")));
  }

  /**
   * Internal implementation for declaring interfaces in this containing file.
   *
   * @param <T> Inferred type to drive which the instance should request from.
   */
  @lombok.Value
  @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
  class Impl<T> implements Client<T>,
                           ThenSerialize,
                           ThenFilter<T> {

    private static final Gson MAPPER = new Gson();
    HttpClient client = HttpClient
        .newBuilder().version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(1)).build();
    TypeToken<Map<UUID, T>> toMap = new TypeToken<>() {
    };

    URI base;
    Request.RequestBuilder req;

    @Override
    public <U extends Domain<U>> Client<U> compose(final @NonNull UUID id,
                                                   final @NonNull Class<U> ref) {
      val uri = base + "/" + id + "/" + ref.getSimpleName().toLowerCase();
      return new Impl<>(URI.create(uri), req);
    }

    // Basic set

    @Override
    public ThenSerialize request(
        final @lombok.NonNull UnaryOperator<Request.RequestBuilder> req) {
      return new Impl<>(base, req.apply(Request.builder()));
    }

    @Override
    @lombok.SneakyThrows
    public HttpResponse<String> get() {
      val cfg = this.req.build();
      val body = null == cfg.body
          ? BodyPublishers.noBody()
          : BodyPublishers.ofString(MAPPER.toJson(cfg.body));
      val params = null == cfg.params ? "" : paramsOf("&", cfg.params);
      val str = null != cfg.uri ? base + "/" + cfg.uri : "" + base;
      val uri = Exceptions.ILLEGAL_ARGUMENT
          .trapIn(() -> URI.create(str + "?" + params));
      val req = HttpRequest
          .newBuilder()
          .method(cfg.method.name(), body)
          .uri(uri)
          .header("Accept", MimeTypes.Type.APPLICATION_JSON.name());
      return client.send(req.build(), BodyHandlers.ofString());
    }

    // Specialized requests

    @Override
    public ThenFilter<T> retrieve(final @lombok.NonNull T fq,
                                  final @lombok.NonNull Map<String, String> params) {
      val filters = MAPPER.fromJson(MAPPER.toJson(fq), Map.class);
      params.put("fq", paramsOf(",", filters));
      return new Impl<>(base, req.method(HttpMethod.GET).params(params));
    }

    @Override
    public ThenFilter<T> retrieve(final @lombok.NonNull Map<String, String> p) {
      return new Impl<>(base, req.method(HttpMethod.GET).params(p));
    }

    // Then steps

    @Override
    public <U> U thenTurnInto(final @lombok.NonNull Class<U> ref) {
      return Optional.of(get()).filter(r -> 404 != r.statusCode())
                     .map(HttpResponse::body).filter(s -> !s.isBlank())
                     .map(Exceptions.UNPROCESSABLE_ENTITY
                              .trapIn(b -> MAPPER.fromJson(b, ref)))
                     .orElseThrow(Exceptions.NOT_FOUND);
    }

    @Override
    public Map<UUID, T> thenMap() {
      val ss = Optional.of(get()).filter(r -> 404 != r.statusCode())
                      .map(HttpResponse::body).filter(s -> !s.isBlank())
                      .orElseThrow(Exceptions.NOT_FOUND);
      return Exceptions.UNPROCESSABLE_ENTITY.trapIn(() -> MAPPER
          .fromJson(ss, toMap.getType()));
    }

    private static String paramsOf(final @lombok.NonNull String sep,
                                   final @lombok.NonNull Map<?, ?> map) {
      return map.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                .map(String::valueOf).collect(Collectors.joining(sep));
    }
  }

  /**
   * Request configuration parameters to be used when preforming a request.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @lombok.Builder
  @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
  class Request {
    /**
     * HTTP method.
     */
    @lombok.NonNull HttpMethod method;
    /**
     * HTTP URI.
     */
    Serializable uri;
    /**
     * HTTP request body.
     */
    Object body;
    /**
     * Request's query parameters.
     */
    Map<String, ?> params;
  }
}
