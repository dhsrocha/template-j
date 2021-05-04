package template.feature.address;

import java.util.UUID;
import java.util.function.Predicate;
import lombok.NonNull;
import template.base.contract.CacheManager;
import template.base.contract.Controller;
import template.base.contract.Repository;
import template.base.contract.Service;

/**
 * Address feature controller implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
class AddressController extends Service.Cached<Address, UUID>
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

  @Override
  public Predicate<Address> filter(final @NonNull Address c) {
    return super.filter(c)
                .or(a -> a.getType() == c.getType())
                .or(a -> a.getPlace().equals(c.getPlace()))
                .or(a -> a.getNumber().equals(c.getNumber()))
                .or(a -> a.getNeighbourhood().equals(c.getNeighbourhood()))
                .or(a -> a.getMunicipality().equals(c.getMunicipality()))
                .or(a -> a.getState().equals(c.getState()))
                .or(a -> a.getPostalCode().equals(c.getPostalCode()));
  }
}
