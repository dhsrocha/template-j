package template.base.stereotype;

import static template.base.Exceptions.DOMAIN_VIOLATION;

import java.util.Map;
import java.util.function.Function;

/**
 * Marks a type as application's domain.
 *
 * @param <D> Self-reference for marked type.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">>Diego Rocha</a>
 * @see
 * <a href="https://www.domainlanguage.com/ddd">Domain Driven Design reference</a>
 */
public interface Domain<D extends Domain<D>> extends Comparable<D> {

  /**
   * Meant to preferably mark {@link Enum enums} which index domain invariants
   * provided by {@link Domain#invariants()}.
   */
  interface Invariant {

    String name();
  }

  /**
   * Thrown to indicate an invariant rule violation during {@link Domain}
   * object creation.
   */
  @lombok.Getter
  @lombok.AllArgsConstructor
  final class Violation extends IllegalArgumentException {

    /**
     * Indicates which invariant rule was violated.
     */
    private final transient Invariant violated;
  }

  /**
   * Maps the rules that expects the marked {@link Domain domain type}'s
   * desired state in its creation.
   *
   * @return A non-empty mapped set of {@link Invariant invariant rules}.
   */
  Map<Invariant, Function<D, Boolean>> invariants();

  /**
   * Validate a domain object. Meant to be invoked in the corresponding factory
   * method while is created.
   *
   * @param domain The {@link Domain} to validate.
   * @param <D>    A type marked as {@link Domain}.
   * @return The given domain parameter, after its validation processed.
   * @throws IllegalStateException If provided {@link #invariants() invariant
   *                               rule set} returns empty.
   * @throws Domain.Violation      If any of provided {@link #invariants() rule
   *                               set}'s contents fails.
   */
  static <D extends Domain<D>> D validate(final @lombok.NonNull D domain) {
    DOMAIN_VIOLATION
        .throwIf(IllegalStateException::new, domain.invariants()::isEmpty);
    domain.invariants().forEach((k, v) -> DOMAIN_VIOLATION.throwIf(
        e -> new Violation(k), () -> !v.apply(domain)));
    return domain;
  }
}
