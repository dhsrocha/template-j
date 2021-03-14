package template.infra;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import io.javalin.Javalin;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Scope;
import lombok.NonNull;
import template.Application.Mode;
import template.base.contract.Builder;
import template.base.contract.Routes;
import template.infra.Infra.InfraScope;
import template.infra.Infra.Mod;


@InfraScope
@Component(modules = Mod.class)
public interface Infra extends Supplier<Javalin> {

  @Scope
  @interface InfraScope {
  }

  @Module(includes = Web.class)
  class Mod implements Infra {

    private final @NonNull Javalin server;

    @Inject
    Mod(final Javalin server) {
      this.server = server;
    }

    @Override
    public Javalin get() {
      return server;
    }
  }

  @Component.Builder
  interface Build extends Builder<Infra> {

    @BindsInstance
    Build mode(final @NonNull Mode mode);

    @BindsInstance
    Build routes(final @NonNull Routes routes);
  }
}
