package template.base.contract;

/**
 * The general abstraction for implementing Builder Design Pattern, indicates
 * that the implementation is able to be built.
 *
 * @param <B> The type meant to be built.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Buildable<B> {

  /**
   * Finishes the building chain.
   *
   * @return The built object.
   */
  B build();

  /**
   * Utility interface for implementing {@link Buildable}'s component's first
   * injectable.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">>Diego Rocha</a>
   * @see Buildable.Part2
   */
  interface Part1<P extends Part1<P, B, C>, B, C> extends Buildable<B> {
    @dagger.BindsInstance
    P part1(final @lombok.NonNull C c);
  }

  /**
   * Utility interface for implementing {@link Buildable}'s component's second
   * injectable.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">>Diego Rocha</a>
   * @see Buildable.Part1
   */
  interface Part2<P extends Part2<P, B, C>, B, C> extends Buildable<B> {
    @dagger.BindsInstance
    P part2(final @lombok.NonNull C c);
  }

  /**
   * Utility interface for implementing {@link Buildable}'s component's first
   * dependency.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">>Diego Rocha</a>
   */
  interface Dep1<D extends Dep1<D, B, C>, B, C> extends Buildable<B> {
    D dep1(final @lombok.NonNull C c);
  }
}
