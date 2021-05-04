package template.feature.auth;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.val;
import template.base.Exceptions;
import template.base.contract.CacheManager;
import template.base.contract.Controller;
import template.base.contract.Filter;
import template.base.contract.Repository;
import template.base.contract.Service;
import template.feature.auth.Auth.Role;
import template.feature.user.User;

/**
 * Auth feature's controller.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
final class AuthController extends Service.Cached<Auth, UUID>
    implements Filter<Auth>,
               Controller<Auth> {

  private static final String AUTH = "Authorization";

  private final @lombok.NonNull Service<User, UUID> user;

  @javax.inject.Inject
  AuthController(final @lombok.NonNull CacheManager<Auth, UUID> cache,
                 final @lombok.NonNull Repository.Cached<Auth, UUID> repo,
                 // TODO beware fo cyclic dependency
                 final @lombok.NonNull Service<User, UUID> user) {
    super(cache, repo);
    this.user = user;
  }

  @Override
  public Class<Auth> ref() {
    return Auth.class;
  }

  /**
   * Evaluates {@link Token token} validity. Should be used as a interceptors
   * (used before) for every request.
   *
   * @return {@link Handler the request handler}.
   */
  @Override
  public Handler filter() {
    return Exceptions.FORBIDDEN_ACCESS.trapIn(() -> ctx -> {
      val compact = compactFrom(ctx);
      // TODO handle in some way secret key
      // val token = Token.parse("", compact);
      // TODO apply functional requirements
    });
  }

  /**
   * Register an auth entry for an user.
   */
  @Override
  public UUID create(
      final @NonNull Auth auth) { // Maybe carry User over this object
    //    val compact = Optional.ofNullable(ctx.header(AUTH)).orElseThrow();
    //    val token = Token.fromUser(found);
    //    val found = user.getOne(UUID.randomUUID());
    // TODO apply functional requirements
    // TODO Should extensively support recovering mechanism
    // TODO
    return super.create(auth);
  }

  /**
   * For some management use.
   */
  @Override
  public void getAll(final @NonNull Context ctx) {
    // TODO Should be restricted to ADMIN users
    isAdmin(ctx);
    Controller.super.getAll(ctx);
  }

  /**
   * For some management use.
   */
  @Override
  public void getOne(final @NonNull Context ctx, final @NonNull String id) {
    isAdmin(ctx);
    Controller.super.getOne(ctx, id);
  }

  /**
   * Accepts un confirmation step request. Internally updates "TO_CONFIRM" to
   * "ACTIVE".
   */
  @Override
  public void update(final @NonNull Context ctx, final @NonNull String id) {
    isAdmin(ctx);
    // TODO should require an specific token given by confirmation step (email)
    Controller.super.update(ctx, id);
  }

  @Override
  public boolean update(final @NonNull UUID id, final @NonNull Auth toUpdate) {
    // TODO Should update state from TO_CONFIRM to ACTIVE
    return super.update(id, toUpdate);
  }

  /**
   * Resets an auth register. internally updates from "ACTIVE" to "TO_CONFIRM"
   */
  @Override
  public void delete(final @NonNull Context ctx, final @NonNull String id) {
    isAdmin(ctx);
    Controller.super.delete(ctx, id);
  }

  private static void isAdmin(final @NonNull Context ctx) {
    val compact = Optional.ofNullable(ctx.header(AUTH)).orElseThrow();
    val t = Token.parse("", compact);
    val role = Auth.Role.valueOf(t.getClaims().get("roles").toString());
    Exceptions.FORBIDDEN_ACCESS.throwIf(() -> Role.ADMIN != role);
  }

  private String compactFrom(final @NonNull Context ctx) {
    return Optional.ofNullable(ctx.header(AUTH)).orElseThrow();
  }
}
