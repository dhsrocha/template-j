package template.core;

import io.javalin.apibuilder.ApiBuilder;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.function.Supplier;
import template.Application;
import template.Application.Feat;
import template.Application.Mode;
import template.base.contract.Buildable;
import template.base.contract.Controller;
import template.base.contract.Dao;
import template.base.contract.Router;
import template.core.Routes.Mod;
import template.core.Routes.Scope;
import template.feature.address.Address;
import template.feature.info.Info;
import template.feature.user.User;

/**
 * Component for exposing application's ReST resources. Ultimately, assembles
 * and decides which features should be activated and resolved.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Scope
@Application.Scope
@dagger.Component(modules = Mod.class)
interface Routes extends Supplier<Router> {

  /**
   * Meant to scope elements for {@link Routes routing concerns}.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @javax.inject.Scope
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface Scope {
  }

  /**
   * Type for creating instances managed by Dagger.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @dagger.Module(includes = {Info.Mod.class, User.Mod.class, Address.Mod.class})
  interface Mod {

    @Scope
    @dagger.Provides
    static Router routes(final @lombok.NonNull Application.Mode mode,
                         final @lombok.NonNull Application.Feat[] feats,
                         final @lombok.NonNull Controller.Single<Info> info,
                         final @lombok.NonNull Controller<User> user,
                         final @lombok.NonNull Controller<Address> address,
                         final @lombok.NonNull Controller.Aggregate<User,
                             Address> userAddress) {
      return () -> {
        if (Mode.PRD != mode) {
          ApiBuilder.get(info);
        }
        for (final var f : feats) {
          if (Feat.USER == f) {
            ApiBuilder.crud(user.path(), user);
          }
          if (Feat.ADDRESS == f) {
            ApiBuilder.crud(address.path(), address);
          }
        }
        if (Arrays.stream(feats).filter(
            f -> Feat.USER == f || Feat.ADDRESS == f).limit(2).count() == 2) {
          ApiBuilder.crud(userAddress.path(), userAddress);
        }
      };
    }
  }

  /**
   * Type for composing components which life-cycle are managed by Dagger.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @dagger.Component.Builder
  interface Build extends Buildable.Part1<Build, Routes, Application.Mode>,
                          Buildable.Part2<Build, Routes, Application.Feat[]>,
                          Buildable.Part3<Build, Routes, Dao> {
  }
}
