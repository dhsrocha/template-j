package template.base.contract;

/**
 * General abstraction for implementing Builder Design Pattern.
 *
 * @param <B> The type meant to be built.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">>Diego Rocha</a>
 */
public interface Builder<B> {

  /**
   * Finishes the building chain.
   *
   * @return The built object.
   */
  B build();

  /**
   * Utility interface for implementing {@link Builder}'s first component.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">>Diego Rocha</a>
   * @see Builder.Part2
   */
  interface Part1<P extends Part1<P, B, C>, B, C> extends Builder<B> {
    @dagger.BindsInstance
    P part1(final @lombok.NonNull C c);
  }

  /**
   * Utility interface for implementing {@link Builder}'s second component.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">>Diego Rocha</a>
   * @see Builder.Part1
   */
  interface Part2<P extends Part2<P, B, C>, B, C> extends Builder<B> {
    @dagger.BindsInstance
    P part2(final @lombok.NonNull C c);
  }
}
