package template.feature.address;

import java.util.UUID;
import template.base.contract.Repository;
import template.base.stereotype.Entity;

/**
 * Address feature repository implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
class AddressRepository extends Repository.Default<Address> {

  @javax.inject.Inject
  AddressRepository(final @lombok.NonNull Entity<UUID, Address> entity) {
    super(entity);
  }
}
