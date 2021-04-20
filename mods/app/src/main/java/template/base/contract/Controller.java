package template.base.contract;

import com.google.gson.Gson;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.UUID;
import lombok.val;
import template.base.stereotype.Domain;

/**
 * General module for application's controllers.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Controller<D extends Domain<D>> extends CrudHandler,
                                                         Api<D, UUID>,
                                                         Domain.Ref<D> {

  Gson MAPPER = new Gson();

  default String crudPath() {
    return domainRef().getSimpleName().toLowerCase() + "/:id";
  }

  @Override
  default void create(final @lombok.NonNull Context ctx) {
    val d = Domain.validate(ctx.bodyAsClass(domainRef()));
    ctx.result(MAPPER.toJson(create(d)));
  }

  @Override
  default void getOne(final @lombok.NonNull Context ctx,
                      final @lombok.NonNull String id) {
    ctx.result(MAPPER.toJson(getOne(UUID.fromString(id))));
  }

  @Override
  default void getAll(final @lombok.NonNull Context ctx) {
    val res = ctx.body().isBlank()
        ? getAll() : getBy(ctx.bodyAsClass(domainRef()));
    ctx.result(MAPPER.toJson(res));
  }

  @Override
  default void update(final @lombok.NonNull Context ctx,
                      final @lombok.NonNull String id) {
    val d = Domain.validate(ctx.bodyAsClass(domainRef()));
    ctx.status(update(UUID.fromString(id), d) ? 204 : 404);
  }

  @Override
  default void delete(final @lombok.NonNull Context ctx,
                      final @lombok.NonNull String id) {
    ctx.status(delete(UUID.fromString(id)) ? 204 : 404);
  }

  @Override
  default int count() {
    return getAll().size();
  }

  /**
   * Controller for a returning a specific object, preferably, a value object.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  interface Getter<T> extends Handler {

    @Override
    default void handle(final @lombok.NonNull Context ctx) {
      ctx.result(MAPPER.toJson(get()));
    }

    T get();
  }
}
