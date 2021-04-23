package template.base.stereotype;

import java.util.Set;
import java.util.function.Predicate;
import lombok.val;
import template.base.Exceptions;

/**
 * Marks a type as an application's domain.
 *
 * @param <D> Self-reference for marked type.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 * @see
 * <a href="https://www.domainlanguage.com/ddd">Domain Driven Design reference</a>
 */
public interface Domain<D extends Domain<D>> extends Comparable<D> {

  /**
   * Indicates that the implementing type can refer to given {@link Domain
   * domain type}. Meant to be used along with system-wise abstractions which
   * are generalized and depend on class reference to allow be located by
   * contextual dependency injection mechanism.
   *
   * @param <D> The type reference.
   */
  interface Ref<D extends Domain<D>> {

    /**
     * Indicates a {@link Domain} reference for type inference purposes.
     *
     * @return the class reference.
     */
    Class<D> domainRef();
  }

  /**
   * Meant to preferably mark {@link Enum enums} which index domain invariants
   * provided by {@link Domain#invariants()}.
   */
  interface Invariant<T extends Domain<T>> {

    Predicate<T> getTest();

    String name();
  }

  /**
   * Thrown to indicate an invariant rule violation during {@link Domain}
   * object creation.
   */
  @lombok.Getter
  @lombok.AllArgsConstructor
  class Violation extends RuntimeException {

    private final transient Invariant<?>[] invariants;
  }

  /**
   * Maps the rules that expects the marked {@link Domain domain type}'s
   * desired state in its creation.
   *
   * @return A non-empty mapped set of {@link Invariant invariant rules}.
   */
  Set<Invariant<D>> invariants();

  /**
   * Validate a domain object. Meant to be invoked in the corresponding factory
   * method while is created.
   *
   * @param domain The {@link Domain} to validate.
   * @param <D>    A type marked as {@link Domain}.
   * @return The given domain parameter, after its validation processed.
   * @throws IllegalStateException If provided {@link #invariants() invariant
   *                               rule set} returns empty.
   * @throws Violation             If any of provided {@link #invariants() rule
   *                               set}'s contents fails.
   */
  static <D extends Domain<D>> D validate(final @lombok.NonNull D domain) {
    Exceptions.ILLEGAL_ARGUMENT.throwIf(domain.invariants()::isEmpty);
    val rules = domain.invariants().stream()
                      .filter(e -> !e.getTest().test(domain))
                      .toArray(Invariant[]::new);
    Exceptions.throwIf(() -> new Violation(rules), () -> rules.length != 0);
    return domain;
  }
}
