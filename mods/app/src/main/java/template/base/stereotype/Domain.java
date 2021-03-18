package template.base.stereotype;

import static template.base.Exceptions.INVALID_DOMAIN;

import java.util.Map;
import java.util.function.Function;
import lombok.NonNull;
import template.base.Exceptions;

public interface Domain<D extends Domain<D>> extends Comparable<D> {

  /**
   * Meant to mark {@link Enum enums} which indexes invariants set provided by
   * {@link Domain#invariants()}.
   */
  interface Invariant {

    String name();
  }

  /**
   * Maps validation rules that expects the the marked {@link Domain domain
   * type}'s desired state.
   *
   * @return A map which contains a mapped set of validation rules
   *     {@link Invariant entry} to describe what the logic is about.
   */
  Map<Invariant, Function<D, Boolean>> invariants();

  /**
   * Validate a domain type. Meant to be used at instance factory method.
   *
   * @param domain The {@link Domain} to validate.
   * @param <D>    A type marked as {@link Domain}.
   * @return The given domain parameter, after its validation processed.
   * @throws IllegalArgumentException with {@link Exceptions#INVALID_DOMAIN
   *                                  entry} plus a enum entry marked with
   *                                  {@link Invariant} indicating the
   *                                  validation failure.
   */
  static <D extends Domain<D>> D validate(final @NonNull D domain) {
    final Function<String, RuntimeException> ex = IllegalArgumentException::new;
    INVALID_DOMAIN.throwIf(ex, () -> domain.invariants().isEmpty());
    domain.invariants().forEach((k, v) -> INVALID_DOMAIN.throwIf(
        e -> ex.apply(e + ": " + k.name()), () -> !v.apply(domain)));
    return domain;
  }
}
