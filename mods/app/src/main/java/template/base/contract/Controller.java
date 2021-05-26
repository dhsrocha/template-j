package template.base.contract;

import static template.base.contract.Params.ROOT_ID;

import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.val;
import template.base.Exceptions;
import template.base.contract.Router.Path;
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
    val sourced = getBy(Params.FQ.bodyFrom(ctx, ref()), skip, limit);
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

  /**
   * Aggregates sets of resources from a {@link T specific domain context} to
   * another one's.
   *
   * @param <T> {@link Domain} type which the association will be based on.
   * @param <U> {@link Domain} type to be handled by the following operations.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @apiNote It is intended to have only one implementation per combination of
   *     the inferred types, as the dependency injection framework generates
   *     code considering both type inferences.
   */
  interface Aggregate<T extends Domain<T>, U extends Domain<U>>
      extends CrudHandler,
              Router.Path<T>,
              Composable<T, U, UUID> {

    /**
     * Provides a class reference from the extension domain context.
     *
     * @return The class reference.
     */
    Class<U> extRef();

    /**
     * Standard path for crud operations with aggregated resources.
     *
     * @return String with pattern {@code {2nd-domain}/:id}.
     */
    @Override
    default String path() {
      return ref().getSimpleName().toLowerCase() + "/:" + ROOT_ID
          + "/" + extRef().getSimpleName().toLowerCase() + Path.PATH_ID;
    }

    /**
     * Exposes all resources from the projected domain context that are
     * associated with resources from {@link T root domain context}.
     * <br/>
     * <b>Requirements:</b>
     * <ul>
     *   <li>Root domain resource from first path variable must exist.</li>
     * </ul>
     *
     * @param ctx Application's context.
     */
    @Override
    default void getAll(final @lombok.NonNull Context ctx) {
      final int skip = Params.SKIP.valFrom(ctx, Integer::parseInt)
                                  .filter(i -> i > 0).orElse(0);
      final int limit = Params.LIMIT.valFrom(ctx, Integer::parseInt)
                                    .filter(i -> i > 0).orElse(30);
      Exceptions.ILLEGAL_ARGUMENT.throwIf(Params.MSG, () -> skip > limit);
      val root = Exceptions.INVALID_ID
          .trapIn(() -> UUID.fromString(ctx.pathParam(ROOT_ID)));
      val criteria = Params.FQ.bodyFrom(ctx, extRef());
      ctx.result(Params.MAPPER.toJson(getByFrom(root, criteria, skip, limit)));
    }

    /**
     * Exposes a particular resource from the projected domain context that are
     * associated with resources from root domain context.
     * <br/>
     * <b>Requirements:</b>
     * <ul>
     *   <li>Root domain resource from first path variable must exist;</li>
     *   <li>Projected domain resource from first path variable must exist.</li>
     * </ul>
     *
     * @param ctx Application's context.
     * @param id  Identity key which it must correspond to an existing resource.
     */
    @Override
    default void getOne(final @lombok.NonNull Context ctx,
                        final @lombok.NonNull String id) {
      val root = Exceptions.INVALID_ID
          .trapIn(() -> UUID.fromString(ctx.pathParam(ROOT_ID)));
      val uuid = Exceptions.INVALID_ID.trapIn(() -> UUID.fromString(id));
      ctx.result(Params.MAPPER.toJson(getOneFrom(root, uuid)));
    }

    /**
     * Creates projection's domain context bound to {@link T root domain
     * context}'s provided resource.
     * <br/>
     * <b>Requirements:</b>
     * <ul>
     *   <li>Root domain resource from first path variable must exist.</li>
     * </ul>
     *
     * @param ctx Application's context.
     */
    @Override
    default void create(final @lombok.NonNull Context ctx) {
      val root = Exceptions.INVALID_ID
          .trapIn(() -> UUID.fromString(ctx.pathParam(ROOT_ID)));
      val body = Exceptions.EMPTY_BODY.trapIn(() -> ctx.bodyAsClass(extRef()));
      ctx.status(201);
      ctx.result(Params.MAPPER.toJson(createOn(root, Domain.validate(body))));
    }

    /**
     * Creates correlation between provided identities' resources, only if
     * there is not any.
     * <br/>
     * <b>Requirements:</b>
     * <ul>
     *   <li>Root domain resource from first path variable must exist;</li>
     *   <li>Projected domain resource from first path variable must exist.</li>
     * </ul>
     *
     * @param ctx Application's context.
     * @param id  Identity key which it must correspond to an existing resource.
     */
    @Override
    default void update(final @lombok.NonNull Context ctx,
                        final @lombok.NonNull String id) {
      val root = Exceptions.INVALID_ID
          .trapIn(() -> UUID.fromString(ctx.pathParam(ROOT_ID)));
      val uuid = Exceptions.INVALID_ID.trapIn(() -> UUID.fromString(id));
      Exceptions.CANNOT_BIND_UNBIND.throwIf(() -> !link(root, uuid));
      ctx.status(204);
    }

    /**
     * Removes correlation between provided identities' resources, only if
     * there is any.
     * <br/>
     * <b>Requirements:</b>
     * <ul>
     *   <li>Root domain resource from first path variable must exist;</li>
     *   <li>Projected domain resource from first path variable must exist.</li>
     * </ul>
     *
     * @param ctx Application's context.
     * @param id  Identity key which it must correspond to an existing resource.
     */
    @Override
    default void delete(final @lombok.NonNull Context ctx,
                        final @lombok.NonNull String id) {
      val root = Exceptions.INVALID_ID
          .trapIn(() -> UUID.fromString(ctx.pathParam(ROOT_ID)));
      val uuid = Exceptions.INVALID_ID.trapIn(() -> UUID.fromString(id));
      Exceptions.CANNOT_BIND_UNBIND.throwIf(() -> !unlink(root, uuid));
      ctx.status(204);
    }
  }
}
