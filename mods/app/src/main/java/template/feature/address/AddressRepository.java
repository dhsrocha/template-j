package template.feature.address;

import java.util.HashMap;
import template.base.contract.Repository;

/**
 * Address feature repository implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
class AddressRepository extends Repository.Default<Address> {

  @javax.inject.Inject
  AddressRepository() {
    super(new HashMap<>());
  }
}
