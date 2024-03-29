package template.feature.address;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.AccessLevel;
import template.base.Checks;
import template.base.contract.Buildable;
import template.base.contract.CacheManager;
import template.base.contract.Controller;
import template.base.contract.Repository;
import template.base.contract.Router;
import template.base.contract.Service;
import template.base.stereotype.Domain;

/**
 * {@link Domain} which represents class an address.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Address implements Domain<Address>,
                                Buildable<Address> {

  /**
   * {@link Address} business rules.
   */
  @SuppressWarnings({"ImmutableEnumChecker", "MissingOverride"})
  @lombok.Getter
  @lombok.AllArgsConstructor
  private enum Rules implements Invariant<Address> {
    PLACE_NOT_BLANK(Checks.NOT_BLANK.on(Address::getPlace)),
    NUMBER_NOT_BLANK(Checks.NOT_BLANK.on(Address::getNumber)),
    NEIGHBOURHOOD_NOT_BLANK(Checks.NOT_BLANK.on(Address::getNeighbourhood)),
    MUNICIPALITY_NOT_BLANK(Checks.NOT_BLANK.on(Address::getMunicipality)),
    STATE_NOT_BLANK(Checks.NOT_BLANK.on(Address::getState)),
    POSTAL_CODE_NOT_BLANK(Checks.NOT_BLANK.on(Address::getPostalCode)),
    ;
    private final Predicate<Address> test;
  }

  /**
   * Types of an {@link Address}.
   */
  public enum Type {
    STREET, AVENUE, ROAD,
  }

  /**
   * Address type.
   *
   * @see Type
   */
  @lombok.NonNull Type type;
  /**
   * Place.
   */
  @lombok.NonNull String place;
  /**
   * Number.
   */
  @lombok.NonNull String number;
  /**
   * Neighbourhood.
   */
  @lombok.NonNull String neighbourhood;
  /**
   * Municipality.
   */
  @lombok.NonNull String municipality;
  /**
   * State.
   */
  @lombok.NonNull String state;
  /**
   * Postal code.
   */
  @lombok.NonNull String postalCode;

  private static final Set<Invariant<Address>> SET = Set.of(Rules.values());
  private static final Comparator<Address> COMPARATOR = Comparator
      .comparing(Address::getType, nullsLast(naturalOrder()))
      .thenComparing(Address::getPlace, nullsLast(naturalOrder()))
      .thenComparing(Address::getNumber, nullsLast(naturalOrder()))
      .thenComparing(Address::getNeighbourhood, nullsLast(naturalOrder()))
      .thenComparing(Address::getMunicipality, nullsLast(naturalOrder()))
      .thenComparing(Address::getState, nullsLast(naturalOrder()))
      .thenComparing(Address::getPostalCode, nullsLast(naturalOrder()));

  @Override
  public Address build() {
    return Domain.validate(new Address(type, place, number, neighbourhood,
                                       municipality, state, postalCode));
  }

  @Override
  public Set<Invariant<Address>> invariants() {
    return SET;
  }

  @Override
  public int compareTo(final @lombok.NonNull Address a) {
    return COMPARATOR.compare(this, a);
  }

  /**
   * Type for binding package-private implementations to public interfaces.
   * It is meant to be included into a {@link Router} managed module.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @SuppressWarnings("unused")
  @dagger.Module
  public interface Mod {

    // Controller

    @dagger.Binds
    Controller<Address> controller(final AddressController a);

    @dagger.Binds
    Service<Address, UUID> service(final AddressController a);

    // Repository

    @dagger.Binds
    Repository.Cached<Address, UUID> cached(final AddressRepository a);

    // Caching

    @dagger.Binds
    CacheManager<Address, UUID> cacheManager(final AddressCache a);
  }
}
