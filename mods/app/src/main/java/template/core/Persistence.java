package template.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.sql.DataSource;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import template.Application;
import template.Application.Mode;
import template.base.contract.Buildable;
import template.base.contract.Dao;
import template.core.Persistence.Mod;
import template.core.Persistence.Scope;

/**
 * Component for dealing with persistence layer communication.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Scope
@Application.Scope
@dagger.Component(modules = Mod.class)
interface Persistence extends Supplier<Dao> {

  /**
   * Meant to scope elements for {@link Persistence persisting concerns}.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @javax.inject.Scope
  @Target({ElementType.TYPE, ElementType.METHOD})
  @interface Scope {
  }

  /**
   * Database connection configuration holder.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @lombok.Value
  @lombok.Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  class Config {
    /**
     * Database driver class.
     */
    @lombok.NonNull String driver;
    /**
     * Database URL.
     */
    @lombok.NonNull String url;
    /**
     * Database username.
     */
    @lombok.NonNull String user;
    /**
     * Database password, if any.
     */
    @lombok.NonNull String pwd;
  }

  /**
   * Type for creating instances managed by Dagger.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @dagger.Module
  interface Mod {

    String CACHE = "cachePrepStmts";
    String SIZE = "prepStmtCacheSize";
    String LIMIT = "prepStmtCacheSqlLimit";
    String USE = "useServerPrepStmts";

    @Scope
    @dagger.Provides
    static DataSource ds(final @lombok.NonNull Config c) {
      val cfg = new HikariConfig();
      cfg.setDriverClassName(c.driver);
      cfg.setJdbcUrl(c.url);
      cfg.setUsername(c.user);
      cfg.setPassword(c.pwd);
      cfg.addDataSourceProperty(CACHE, Boolean.TRUE);
      cfg.addDataSourceProperty(SIZE, 250);
      cfg.addDataSourceProperty(LIMIT, 2048);
      cfg.addDataSourceProperty(USE, Boolean.TRUE);
      return new HikariDataSource(cfg);
    }

    @Scope
    @dagger.Provides
    static Dao dao(final @lombok.NonNull Application.Mode m,
                   final @lombok.NonNull DataSource ds) {
      val d = Mode.PRD == m ? SQLDialect.POSTGRES : SQLDialect.HSQLDB;
      return new Dao() {

        @Override
        @lombok.SneakyThrows
        public <T> Mapper<T, UUID> from(final @lombok.NonNull Class<T> ref) {
          return Default.of(DSL.using(ds, d), ref);
        }

        @Override
        @lombok.SneakyThrows
        public <T, U> Mapper.Composed<U, UUID> from(
            final @lombok.NonNull UUID root,
            final @lombok.NonNull Class<T> ref,
            final @lombok.NonNull Class<U> ext,
            final @lombok.NonNull Predicate<U> canBind) {
          return Composed.of(root, canBind, DSL.using(ds, d), ref, ext);
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
  interface Build extends Buildable.Part1<Build, Persistence, Mode>,
                          Buildable.Part2<Build, Persistence, Config> {
  }
}
