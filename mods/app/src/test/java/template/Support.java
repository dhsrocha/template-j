package template;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.hsqldb.jdbcDriver;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.bridge.SLF4JBridgeHandler;
import template.Application.Feat;
import template.Application.Mode;
import template.base.Exceptions;
import template.core.Bootstrap;
import template.core.Props;

/**
 * Supporting assets for testing purposes.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Support {

  /**
   * Activates {@link Bootstrap#bootstrap(String...)} to test endpoints under
   * <b>integration testing strategy</b>. Should preferably be used along with
   * {@link Client}.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @ExtendWith({LogExtension.class, DbExtension.class, AppExtension.class})
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @interface IntegrationTest {
    /**
     * Features to activate on application startup. Default value means loading
     * up {@link Feat#values() all features available}. Values added to
     * {@link #activated()} method have higher topmost priority.
     *
     * @return Feature entries.
     * @see #activated()
     */
    Feat[] value() default {};

    /**
     * Features to activate on application startup. Default value means loading
     * up {@link Feat#values() all features available}. Values added here have
     * higher topmost priority.
     *
     * @return Feature entries.
     */
    Feat[] activated() default {};

    /**
     * Indicates the database's migration file path.
     *
     * @return The file's path.
     */
    String migrationPath() default "db-changelog.xml";
  }

  /**
   * Configures logging level for the annotated asset.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @interface Logged {
    /**
     * Defines the log level to the scoped asset under execution. Defaults to
     * {@link org.slf4j.event.Level#ERROR}.
     *
     * @return The logging level.
     */
    org.slf4j.event.Level value() default org.slf4j.event.Level.ERROR;
  }

  // ::: Helpers :::

  /**
   * Common listening port shared by the starting up application and its
   * requesting clients.
   */
  int PORT = nextAvailablePort();

  /**
   * Recursively loops until finding a available TCP port, locks it, get its
   * value and then releases it.
   *
   * @return next available port.
   */
  static int nextAvailablePort() {
    try (val s = new ServerSocket(0)) {
      return s.getLocalPort();
    } catch (final IOException e) {
      return nextAvailablePort();
    }
  }

  /**
   * Creates a {@link PrintWriter} based on the provided {@link Consumer}
   * function. Meant to be used on the middleware applications' loggers applied
   * in the test fixtures.
   *
   * @param log Preferably, a consuming {@link org.slf4j.Logger} function.
   * @return The {@link PrintWriter}.
   */
  private static PrintWriter writerFor(final @NonNull Consumer<String> log) {
    return new PrintWriter(new ByteArrayOutputStream()) {
      @Override
      public void println(final String s) {
        log.accept(s);
      }
    };
  }

  /**
   * Redirect logging to SLF4J.
   *
   * @param wrap Computation to be wrapped.
   */
  private static void slf4jOn(
      final @NonNull Exceptions.CheckedRunnable<?> wrap) {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    Exceptions.ILLEGAL_ARGUMENT.trapIn(wrap);
    SLF4JBridgeHandler.uninstall();
  }

  // ::: Extensions :::

  /**
   * JUnit extension to start to control logging during the testing.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  final class LogExtension implements BeforeTestExecutionCallback,
                                      BeforeEachCallback {
    private static final Level INITIAL = setLevel(Level.OFF);

    @Override
    public void beforeEach(final ExtensionContext test) {
      test.getTestMethod()
          .map(a -> a.getAnnotation(Logged.class))
          .map(a -> Level.toLevel(a.value().name()))
          .filter(l -> !l.equals(INITIAL))
          .ifPresent(LogExtension::setLevel);
    }

    @Override
    public void beforeTestExecution(final ExtensionContext test) {
      test.getTestInstance()
          .map(Object::getClass)
          .map(c -> c.getAnnotation(Logged.class))
          .map(a -> Level.toLevel(a.value().name()))
          .filter(l -> !l.equals(INITIAL))
          .ifPresent(LogExtension::setLevel);
    }

    private static Level setLevel(final @NonNull Level level) {
      val ctxLog = (LoggerContext) LogManager.getContext(Boolean.FALSE);
      val cfg = ctxLog.getConfiguration()
                      .getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
      val current = cfg.getLevel();
      cfg.setLevel(level);
      ctxLog.updateLoggers();
      return current;
    }
  }

  /**
   * JUnit extension to start up {@link Bootstrap application's bootstrap}.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  final class AppExtension implements BeforeTestExecutionCallback,
                                      AfterTestExecutionCallback {
    private static final AtomicReference<Application.Server> REF =
        new AtomicReference<>();

    @Override
    public void beforeTestExecution(final ExtensionContext ctx) {
      val feats = ctx.getTestInstance()
                     .map(Object::getClass)
                     .map(a -> a.getAnnotation(IntegrationTest.class))
                     .map(a -> Optional.of(a.activated())
                                       .filter(l -> l.length > 0)
                                       .orElse(a.value()))
                     .filter(l -> l.length > 0)
                     .orElseGet(Feat::values);
      REF.set(Bootstrap.bootstrap(
          Props.MODE.is(Mode.TEST),
          Props.PORT.is(Support.PORT),
          Props.FEAT.is(Arrays.toString(feats).replaceAll("[\\[\\] ]", "")),
          Props.DB_DRIVER.is(jdbcDriver.class.getCanonicalName()),
          Props.DB_URL.is(DbExtension.DB_URL),
          Props.DB_USER.is(DbExtension.DB_USER),
          Props.DB_PWD.is(DbExtension.DB_PWD)));
    }

    @Override
    public void afterTestExecution(final ExtensionContext ctx) {
      REF.get().stop();
    }
  }

  /**
   * JUnit extension to start up in-memory database at server mode and apply
   * migration schema on it.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @Slf4j
  final class DbExtension implements BeforeAllCallback,
                                     BeforeTestExecutionCallback,
                                     AfterTestExecutionCallback,
                                     AfterAllCallback {
    private static final Server SERVER = new Server();
    // Params
    public static final String DB_URL = "jdbc:hsqldb:mem:main";
    public static final String DB_USER = "user";
    public static final String DB_PWD = "";

    @Override
    public void beforeAll(final ExtensionContext ctx) throws Exception {
      SERVER.setLogWriter(writerFor(log::info));
      SERVER.setErrWriter(writerFor(log::error));
      SERVER.setDatabaseName(0, "main");
      SERVER.setDatabasePath(0, "mem:db");
      SERVER.setPort(nextAvailablePort());
      SERVER.setSilent(Boolean.TRUE);
      val props = new HsqlProperties();
      props.setProperty("sql.syntax_pgs", Boolean.TRUE);
      SERVER.setProperties(props);
      SERVER.start();
      val now = System.currentTimeMillis();
      while (SERVER.isNotRunning() && System.currentTimeMillis() - now < 50) {
        log.trace("Waiting DB for starting up.");
      }
      Exceptions.ILLEGAL_ARGUMENT.throwIf(
          "In-memory server not ready.", () -> SERVER.getState() != 1);
    }

    @Override
    public void beforeTestExecution(final ExtensionContext ctx) {
      Migrator.create(ctx).act(l -> slf4jOn(() -> l.update(new Contexts(""))));
    }

    @Override
    public void afterTestExecution(final ExtensionContext ctx) {
      Migrator.create(ctx).act(l -> slf4jOn(l::dropAll));
    }

    @Override
    public void afterAll(final ExtensionContext ctx) {
      Optional.of(SERVER).filter(s -> s.getState() == 1)
              .ifPresent(Server::shutdown);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Migrator {

      private final Path path;

      @SneakyThrows
      private static Migrator create(final @NonNull ExtensionContext ctx) {
        val path = ctx.getTestInstance().map(Object::getClass)
                      .map(a -> a.getAnnotation(IntegrationTest.class))
                      .map(IntegrationTest::migrationPath)
                      .filter(Predicate.not(String::isBlank))
                      .map(p -> Paths.get("src", "main", "resources", p))
                      .orElseThrow(Exceptions.RESOURCE_NOT_FOUND);
        return new Migrator(path);
      }

      @SneakyThrows
      private void act(final @NonNull Consumer<Liquibase> action) {
        val acc = new FileSystemResourceAccessor(path.getParent().toFile());
        val conn = new JdbcConnection(
            DriverManager.getConnection(DB_URL, DB_USER, DB_PWD));
        @Cleanup val lb = new Liquibase(path.toString(), acc, conn);
        action.accept(lb);
      }
    }
  }
}
