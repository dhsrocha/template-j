package template.base.contract;

import com.google.gson.Gson;
import io.javalin.http.Context;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.val;
import template.base.Exceptions;

/**
 * Application's query parameters.
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
  static final String MSG = "Skip parameter is higher than limit parameter.";
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
  <T> Optional<T> parsedFrom(final @lombok.NonNull Context ctx,
                             final @lombok.NonNull Class<T> ref) {
    return valFrom(ctx, Exceptions.ILLEGAL_ARGUMENT.trapIn(s -> {
      val m = MAPPER.fromJson("{" + s + "}", Map.class);
      return MAPPER.fromJson(Params.MAPPER.toJson(m), ref);
    }));
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

  /**
   * Default Predicate which always returns true.
   *
   * @param <T> Any inferred type which is request from typing erasure.
   * @return An predicate which is returns true.
   */
  static <T> Predicate<T> noFilter() {
    return t -> Boolean.TRUE;
  }
}
