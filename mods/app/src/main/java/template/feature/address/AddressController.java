package template.feature.address;

import java.util.UUID;
import lombok.NonNull;
import template.base.contract.CacheManager;
import template.base.contract.Controller;
import template.base.contract.Repository;

/**
 * Address feature controller implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
class AddressController
    extends Controller.Cached<Address> implements Controller<Address> {

  @javax.inject.Inject
  AddressController(final @NonNull CacheManager<Address, UUID> cache,
                    final @NonNull Repository.Cached<Address, UUID> repo) {
    super(cache, repo);
  }

  @Override
  public Class<Address> domainRef() {
    return Address.class;
  }
}
