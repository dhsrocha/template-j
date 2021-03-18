package template;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.javalin.apibuilder.ApiBuilder;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.function.Supplier;
import javax.inject.Scope;
import lombok.NonNull;
import template.Application.Feat;
import template.Router.FeatureScope;
import template.Router.Mod;
import template.base.contract.Routes;

@FeatureScope
@Component(modules = Mod.class)
interface Router extends Supplier<Routes> {

  @Scope
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface FeatureScope {
  }

  @Module
  interface Mod {

    @Provides
    @FeatureScope
    static Routes routes(final @NonNull Feat[] feats) {
      return () -> ApiBuilder.get(ctx -> ctx.result(Arrays.toString(feats)));
    }
  }

  @Component.Builder
  interface Build extends Supplier<Router> {

    @BindsInstance
    Build feats(final @NonNull Feat[] app);
  }
}
