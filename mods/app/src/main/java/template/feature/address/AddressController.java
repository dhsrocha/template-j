package template.feature.address;

import java.util.UUID;
import lombok.NonNull;
import template.base.contract.CacheManager;
import template.base.contract.Controller;
import template.base.contract.Repository;
import template.base.contract.Service;

/**
 * {@link Address} feature controller implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
final class AddressController extends Service.Cached<Address, UUID>
    implements Controller<Address> {

  @javax.inject.Inject
  AddressController(final @NonNull CacheManager<Address, UUID> cache,
                    final @NonNull Repository.Cached<Address, UUID> repo) {
    super(cache, repo);
  }

  @Override
  public Class<Address> ref() {
    return Address.class;
  }
}
