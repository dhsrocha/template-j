package template.feature.auth;

import java.util.UUID;
import template.base.contract.Repository;
import template.base.stereotype.Entity;

final class AuthRepository extends Repository.Default<Auth> {

  @javax.inject.Inject
  AuthRepository(final @lombok.NonNull Entity<UUID, Auth> store) {
    super(store);
  }
}
