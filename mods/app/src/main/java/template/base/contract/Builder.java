package template.base.contract;

public interface Builder<B> {

  B build();

  interface Part1<P extends Part1<P, B, C>, B, C> extends Builder<B> {
    @dagger.BindsInstance
    P part1(final @lombok.NonNull C c);
  }

  interface Part2<P extends Part2<P, B, C>, B, C> extends Builder<B> {
    @dagger.BindsInstance
    P part2(final @lombok.NonNull C c);
  }
}
