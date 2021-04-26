package template.feature.address;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.AccessLevel;
import template.base.contract.Builder;
import template.base.contract.CacheManager;
import template.base.contract.Controller;
import template.base.contract.Repository;
import template.base.contract.Routes;
import template.base.stereotype.Domain;

/**
 * Domain which represents class an address.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Address implements Domain<Address>,
                                Builder<Address> {

  /**
   * {@link Address} business rules.
   */
  @SuppressWarnings({"ImmutableEnumChecker", "MissingOverride"})
  @lombok.Getter
  @lombok.AllArgsConstructor
  private enum Rules implements Invariant<Address> {
    PLACE_IS_NOT_BLANK(a -> !a.place.isBlank()),
    NUMBER_IS_NOT_BLANK(a -> !a.number.isBlank()),
    NEIGHBOURHOOD_IS_NOT_BLANK(a -> !a.neighbourhood.isBlank()),
    MUNICIPALITY_IS_NOT_BLANK(a -> !a.municipality.isBlank()),
    STATE_IS_NOT_BLANK(a -> !a.state.isBlank()),
    POSTAL_IS_NOT_BLANK(a -> !a.postalCode.isBlank()),
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
   * It is meant to be included into a {@link Routes} managed module.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @SuppressWarnings("unused")
  @dagger.Module
  public interface Mod {

    @dagger.Binds
    CacheManager<Address, UUID> cacheManager(final AddressCache u);

    @dagger.Binds
    Repository.Cached<Address, UUID> repository(final AddressRepository u);

    @dagger.Binds
    Controller<Address> controller(final AddressController u);
  }
}
