package template.base.contract;

import com.google.gson.Gson;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.val;
import template.base.Exceptions;
import template.base.stereotype.Domain;

/**
 * General module for application's controllers.
 *
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

  /**
   * Controller for a returning a specific object, preferably, a value object.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  interface Getter<T> extends Handler,
                              Supplier<T> {

    @Override
    default void handle(final @lombok.NonNull Context ctx) {
      ctx.result(MAPPER.toJson(get()));
    }
  }

  /**
   * Default {@link Controller} abstraction with caching capability.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @lombok.AllArgsConstructor(access = AccessLevel.PROTECTED)
  abstract class Cached<D extends Domain<D>> implements Controller<D> {

    private final CacheManager<D, UUID> cache;
    private final Repository.Cached<D, UUID> repo;

    @Override
    public D getOne(final @lombok.NonNull UUID id) {
      return repo.with(cache.from(domainRef())).getOne(id)
                 .orElseThrow(Exceptions.RESOURCE_NOT_FOUND::create);
    }

    @Override
    public Map<UUID, D> getBy(final @lombok.NonNull D criteria) {
      return repo.getMany(criteria);
    }

    @Override
    public Map<UUID, D> getAll() {
      return repo.getAll();
    }

    @Override
    public UUID create(final @lombok.NonNull D user) {
      return repo.with(cache.from(domainRef())).create(user);
    }

    @Override
    public boolean update(final @lombok.NonNull UUID id,
                          final @lombok.NonNull D user) {
      return repo.with(cache.from(domainRef())).update(id, user);
    }

    @Override
    public boolean delete(final @lombok.NonNull UUID id) {
      return repo.with(cache.from(domainRef())).delete(id);
    }
  }
}
