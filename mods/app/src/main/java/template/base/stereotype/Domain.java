package template.base.stereotype;

import io.javalin.http.HttpResponseException;
import java.util.Map;
import java.util.function.Function;
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
  final class Violation extends HttpResponseException {

    Violation(final Invariant violated) {
      super(422, "Violation rule broken.", Map.of("violation", violated.name()));
    }
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
    Exceptions.ILLEGAL_ARGUMENT.throwIf(domain.invariants()::isEmpty);
    domain.invariants().forEach((k, v) -> Exceptions.throwIf(
        () -> new Violation(k), () -> !v.apply(domain)));
    return domain;
  }
}
