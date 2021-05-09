package template.feature.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.UUID;
import template.base.contract.CacheManager;
import template.base.contract.Controller;
import template.base.contract.Repository;
import template.base.contract.Service;
import template.feature.address.Address;

/**
 * {@link User} feature controller implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Tags(@Tag(name = "User"))
@Schema(title = "User Controller")
final class UserController extends Service.Cached<User, UUID>
    implements Controller<User> {

  @javax.inject.Inject
  UserController(final @lombok.NonNull CacheManager<User, UUID> cache,
                 final @lombok.NonNull Repository.Cached<User, UUID> repo) {
    super(cache, repo);
  }

  @Operation()
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Successfully updated schema"),
      @ApiResponse(
          responseCode = "404",
          description = "Schema not found"),
      @ApiResponse(
          responseCode = "400",
          description = "Missing or invalid request body"),
      @ApiResponse(
          responseCode = "500",
          description = "Internal error")
  })
  @Override
  public Class<User> ref() {
    return User.class;
  }

  /**
   * Aggregate implementation between {@link User} and {@link Address} domains.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  static final class WithAddress
      extends Service.Composed<User, Address, UUID>
      implements Controller.Aggregate<User, Address> {

    @javax.inject.Inject
    WithAddress(
        final @lombok.NonNull Repository.Composable<User, Address, UUID> base) {
      super(base);
    }

    @Override
    public Class<User> ref() {
      return User.class;
    }

    @Override
    public Class<Address> extRef() {
      return Address.class;
    }
  }
}
