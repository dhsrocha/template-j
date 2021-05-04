package template.feature.address;

import java.util.UUID;
import template.base.contract.CacheManager;

/**
 * Address feature cache manager implementation.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
class AddressCache extends CacheManager.Default<Address, UUID> {

  @javax.inject.Inject
  AddressCache() {
  }

  @Override
  protected Class<UUID> idRef() {
    return UUID.class;
  }


  @Override
  public Class<Address> ref() {
    return Address.class;
  }
}
