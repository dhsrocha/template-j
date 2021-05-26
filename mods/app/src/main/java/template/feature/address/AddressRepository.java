package template.feature.address;

import template.base.contract.Dao;
import template.base.contract.Repository;

/**
 * {@link Address} feature repository implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
final class AddressRepository extends Repository.Default<Address> {

  @javax.inject.Inject
  AddressRepository(final @lombok.NonNull Dao dao) {
    super(dao, Address.class);
  }
}
