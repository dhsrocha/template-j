package template.base.contract;

import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
    val uuid = Exceptions.ILLEGAL_ARGUMENT.trapIn(() -> UUID.fromString(id));
    ctx.result(Support.MAPPER.toJson(getOne(uuid)));
  }

  @Override
  default void getAll(final @lombok.NonNull Context ctx) {
    final int skip = Support.intOf(ctx, Support.SKIP).orElse(0);
    final int limit = Support.intOf(ctx, Support.LIMIT).orElse(30);
    val str = Optional.ofNullable(ctx.queryParam(Support.FQ)).orElse("");
    final Predicate<D> filter = str
        .isBlank() ? t -> Boolean.TRUE : Exceptions.ILLEGAL_ARGUMENT
        .trapIn(() -> filter(Support.objOf(str, domainRef())));
    val sourced = getBy(filter, skip, limit);
    val sorted = new TreeMap<UUID, D>(Comparator.comparing(sourced::get));
    sorted.putAll(sourced);
    ctx.result(Support.MAPPER.toJson(sorted));
  }

  @Override
  default void update(final @lombok.NonNull Context ctx,
                      final @lombok.NonNull String id) {
    val uuid = Exceptions.ILLEGAL_ARGUMENT.trapIn(() -> UUID.fromString(id));
    val d = Domain.validate(ctx.bodyAsClass(domainRef()));
    ctx.status(update(uuid, d) ? 204 : 404);
  }

  @Override
  default void delete(final @lombok.NonNull Context ctx,
                      final @lombok.NonNull String id) {
    val uuid = Exceptions.ILLEGAL_ARGUMENT.trapIn(() -> UUID.fromString(id));
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
      ctx.result(Support.MAPPER.toJson(get()));
    }
  }
}
