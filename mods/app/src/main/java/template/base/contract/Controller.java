package template.base.contract;

import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.UUID;
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
                                                         Router.Path<D> {

  /**
   * Creates a resource into a {@link D domain} context.
   * <br/>
   * <b>Requirements:</b>
   * <ul>
   *   <li>Request body must not be empty;</li>
   *   <li>Request body must contain related attributes as JSON properties
   *   and corresponding value according to domain's each indexed
   *   {@link Domain.Invariant}.</li>
   * </ul>
   *
   * @param ctx Application's context.
   */
  @Override
  default void create(final @lombok.NonNull Context ctx) {
    val body = Exceptions.EMPTY_BODY.trapIn(() -> ctx.bodyAsClass(ref()));
    ctx.status(201);
    ctx.result(create(Domain.validate(body)).toString());
  }

  /**
   * Retrieves a resource in a domain context identified by provided identity
   * parameter.
   *
   * @param ctx Application's context.
   * @param id  Identity key which it must correspond to an existing resource.
   */
  @Override
  default void getOne(final @lombok.NonNull Context ctx,
                      final @lombok.NonNull String id) {
    val uuid = Exceptions.INVALID_ID.trapIn(() -> UUID.fromString(id));
    ctx.result(Params.MAPPER.toJson(getOne(uuid)));
  }

  /**
   * Retrieves all resources from a domain context. Query parameters can
   * optionally be sent for filtering purposes.
   * <br/>
   * Requirements:
   * <ul>
   *   <li>Filter query must correspond to domain's attributes;</li>
   *   <li>Query parameter {@code limit} must be positive and higher than
   *   {@code skip}.</li>
   * </ul>
   *
   * @param ctx Application's context.
   */
  @Override
  default void getAll(final @lombok.NonNull Context ctx) {
    val skip = Params.SKIP.valFrom(ctx, Integer::parseInt)
                          .filter(i -> i > 0).orElse(0);
    val limit = Params.LIMIT.valFrom(ctx, Integer::parseInt)
                            .filter(i -> i > 0).orElse(30);
    Exceptions.ILLEGAL_ARGUMENT.throwIf(Params.MSG, () -> skip > limit);
    val filter = Params.FQ.parsedFrom(ctx, ref())
                          .map(this::filter).orElseGet(Params::noFilter);
    val sourced = getBy(filter, skip, limit);
    val sorted = new TreeMap<UUID, D>(Comparator.comparing(sourced::get));
    sorted.putAll(sourced);
    ctx.result(Params.MAPPER.toJson(sorted));
  }

  /**
   * Updates a resource in a domain context.
   *
   * @param ctx Application's context.
   * @param id  Identity key which it must correspond to an existing resource.
   */
  @Override
  default void update(final @lombok.NonNull Context ctx,
                      final @lombok.NonNull String id) {
    val uuid = Exceptions.INVALID_ID.trapIn(() -> UUID.fromString(id));
    Exceptions.EMPTY_BODY.throwIf(() -> ctx.body().isBlank());
    val d = Domain.validate(ctx.bodyAsClass(ref()));
    ctx.status(update(uuid, d) ? 204 : 404);
  }

  /**
   * Deletes a resource in a domain context.
   *
   * @param ctx Application's context.
   * @param id  Identity key which it must correspond to an existing resource.
   */
  @Override
  default void delete(final @lombok.NonNull Context ctx,
                      final @lombok.NonNull String id) {
    val uuid = Exceptions.INVALID_ID.trapIn(() -> UUID.fromString(id));
    ctx.status(delete(uuid) ? 204 : 404);
  }

  /**
   * Controller for a returning a specific object, preferably, a value object.
   *
   * @param <T> A type to be used as the response body.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  interface Single<T> extends Handler,
                              Supplier<T>,
                              Router.Path<T> {

    /**
     * Treats return from {@link #get()} in its serialized form as the response
     * body.
     *
     * @param ctx Application's context.
     */
    @Override
    default void handle(final @lombok.NonNull Context ctx) {
      ctx.result(Params.MAPPER.toJson(get()));
    }
  }
}
