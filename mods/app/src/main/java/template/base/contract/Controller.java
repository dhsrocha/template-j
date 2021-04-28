package template.base.contract;

import com.google.gson.Gson;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.val;
import template.base.Exceptions;
import template.base.stereotype.Domain;

/**
 * Generically represents the application's ways for handling domain instances.
 *
 * @param <D> {@link Domain} type to be handled among the operations.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Controller<D extends Domain<D>> extends CrudHandler,
                                                         Service<D, UUID>,
                                                         Domain.Ref<D> {

  Gson MAPPER = new Gson();

  default String crudPath() {
    return domainRef().getSimpleName().toLowerCase() + "/:id";
  }

  @Override
  default void create(final @lombok.NonNull Context ctx) {
    val d = Domain.validate(ctx.bodyAsClass(domainRef()));
    ctx.status(201);
    ctx.result(create(d).toString());
  }

  @Override
  default void getOne(final @lombok.NonNull Context ctx,
                      final @lombok.NonNull String id) {
    val uuid = Exceptions.ILLEGAL_ARGUMENT.trapFrom(() -> UUID.fromString(id));
    ctx.result(MAPPER.toJson(getOne(uuid)));
  }

  @Override
  default void getAll(final @lombok.NonNull Context ctx) {
    val sourced = ctx.queryParamMap()
                     .isEmpty() ? getAll() : Exceptions.ILLEGAL_ARGUMENT
        .trapFrom(() -> {
          val map = ctx
              .queryParamMap().entrySet().stream()
              .map(e -> Map.entry(e.getKey(), e.getValue().get(0)))
              .filter(e -> Objects.nonNull(e.getValue()))
              .collect(
                  Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
          return getBy(
              filter(MAPPER.fromJson(MAPPER.toJson(map), domainRef())));
        });
    val sorted = new TreeMap<UUID, D>(Comparator.comparing(sourced::get));
    sorted.putAll(sourced);
    ctx.result(MAPPER.toJson(sorted));
  }

  @Override
  default void update(final @lombok.NonNull Context ctx,
                      final @lombok.NonNull String id) {
    val uuid = Exceptions.ILLEGAL_ARGUMENT.trapFrom(() -> UUID.fromString(id));
    val d = Domain.validate(ctx.bodyAsClass(domainRef()));
    ctx.status(update(uuid, d) ? 204 : 404);
  }

  @Override
  default void delete(final @lombok.NonNull Context ctx,
                      final @lombok.NonNull String id) {
    val uuid = Exceptions.ILLEGAL_ARGUMENT.trapFrom(() -> UUID.fromString(id));
    ctx.status(delete(uuid) ? 204 : 404);
  }

  /**
   * Controller for a returning a specific object, preferably, a value object.
   *
   * @param <T> A type to be used as the response body.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  interface Getter<T> extends Handler,
                              Supplier<T> {

    @Override
    default void handle(final @lombok.NonNull Context ctx) {
      ctx.result(MAPPER.toJson(get()));
    }
  }
}
