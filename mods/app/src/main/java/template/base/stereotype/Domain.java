package template.base.stereotype;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
                      .collect(Collectors.<Invariant<?>>toUnmodifiableList());
    Exceptions.throwIf(() -> new Violation(rules), () -> rules.size() != 0);
    return domain;
  }

  /**
   * Maps the rules that expects the marked {@link Domain domain type}'s
   * desired state in its creation.
   *
   * @return A non-empty mapped set of {@link Invariant invariant rules}.
   */
  Set<Invariant<D>> invariants();

  /**
   * Meant to preferably mark {@link Enum enums} which index domain invariants
   * provided by {@link Domain#invariants()}.
   *
   * @param <T> A {@link Domain} type.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  interface Invariant<T extends Domain<T>> {

    /**
     * Provides invariant representation.
     *
     * @return A logical test which represents a business rule.
     */
    Predicate<T> getTest();
  }

  /**
   * Thrown to indicate an invariant rule violation during {@link Domain}
   * object creation.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @lombok.Getter
  @lombok.AllArgsConstructor
  class Violation extends RuntimeException {

    private final transient Collection<Invariant<?>> invariants;
  }
}
