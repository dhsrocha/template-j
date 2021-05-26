package template.base.contract;

import com.google.gson.Gson;
import io.javalin.http.Context;
import java.util.Optional;
import java.util.function.Function;
import template.base.Body;

/**
 * Application's query parameter parser.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
enum Params {
  /**
   * Filter query.
   */
  FQ,
  /**
   * Entries from the first to skip.
   */
  SKIP,
  /**
   * Entries up to limit to.
   */
  LIMIT,
  ;
  private static final String EMPTY = "{}";
  static final String MSG = "Skip parameter is higher than limit parameter.";
  static final String ROOT_ID = "root";
  static final Gson MAPPER = new Gson();

  /**
   * Extracts a query parameter from {@link Context} and parses it. It should
   * correspond to a comma-separated {@code k=v} pattern string.
   *
   * @param ctx Application's context.
   * @param ref Type to parse to.
   * @param <T> Value extracted and it should be returned.
   * @return Optionally extracted query parameter.
   */
  <T> Body<T> bodyFrom(final @lombok.NonNull Context ctx,
                       final @lombok.NonNull Class<T> ref) {
    return Body.of(valFrom(ctx, s -> "{" + s + "}").orElse(EMPTY), ref);
  }

  /**
   * Extracts a query parameter from {@link Context}.
   *
   * @param ctx Application's context.
   * @param fun Functor for mapping.
   * @param <T> Value extracted and it should be returned.
   * @return Optionally extracted query parameter.
   */
  <T> Optional<T> valFrom(final @lombok.NonNull Context ctx,
                          final @lombok.NonNull Function<String, T> fun) {
    return Optional.ofNullable(ctx.queryParam(name().toLowerCase())).map(fun);
  }
}
