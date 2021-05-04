package template.base.stereotype;

/**
 * Indicates that the extending type is able to refer to the provided  type.
 * Meant to be used along with system-wise abstractions which are generalized
 * (type-inferred) and depend on a class reference to allow be located by
 * contextual dependency injection mechanism (e.g. Dagger).
 *
 * @param <T> The type reference.
 */
public interface Referable<T> {

  /**
   * Indicates a reference for type inference purposes.
   *
   * @return the class reference.
   */
  Class<T> ref();
}
