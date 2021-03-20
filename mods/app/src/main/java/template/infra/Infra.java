package template.infra;

import dagger.Component;
import dagger.Module;
import io.javalin.Javalin;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
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
  @Target({ElementType.TYPE, ElementType.METHOD})
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
  interface Build extends Builder.Part1<Build, Infra, Mode>,
                          Builder.Part2<Build, Infra, Routes> {
  }
}
