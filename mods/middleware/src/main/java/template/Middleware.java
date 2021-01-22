package template;

import com.github.dockerjava.api.model.ContainerSpec;
import com.github.dockerjava.api.model.EndpointSpec;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.NetworkAttachmentConfig;
import com.github.dockerjava.api.model.PortConfig;
import com.github.dockerjava.api.model.PortConfig.PublishMode;
import com.github.dockerjava.api.model.ServiceGlobalModeOptions;
import com.github.dockerjava.api.model.ServiceModeConfig;
import com.github.dockerjava.api.model.ServiceReplicatedModeOptions;
import com.github.dockerjava.api.model.ServiceSpec;
import com.github.dockerjava.api.model.TaskSpec;
import com.github.dockerjava.api.model.UpdateConfig;
import com.github.dockerjava.api.model.UpdateFailureAction;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.val;

/**
 * Profiles and describes middleware services.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("ImmutableEnumChecker")
@lombok.AllArgsConstructor
enum Middleware {
  RDB("postgres",
      List.of(),
      ServiceMode.GLOBAL,
      List.of(Mounts.SOCKET, Mounts.RDB),
      List.of(Port.RDB),
      List.of(Network.RDB),
      List.of("POSTGRES_DB=" + Credentials.DB_NAME,
              "POSTGRES_USER=" + Credentials.DB_USER,
              "POSTGRES_PASSWORD=" + Credentials.DB_PASS),
      List.of()),
  RDB_CLIENT("adminer",
             List.of(RDB),
             ServiceMode.GLOBAL,
             List.of(Mounts.SOCKET),
             List.of(Port.RDB_CLIENT),
             List.of(Network.RDB),
             List.of("ADMINER_DEFAULT_SERVER=" + RDB),
             List.of()),
  NDB("mongo",
      List.of(),
      ServiceMode.GLOBAL,
      List.of(Mounts.SOCKET, Mounts.NDB, Mounts.NDB_CFG),
      List.of(Port.NDB),
      List.of(Network.NDB),
      List.of("MONGO_INITDB_ROOT_USERNAME=" + Credentials.DB_USER,
              "MONGO_INITDB_ROOT_PASSWORD=" + Credentials.DB_PASS),
      List.of()),
  NDB_CLIENT("mongo-express",
             List.of(NDB),
             ServiceMode.GLOBAL,
             List.of(Mounts.SOCKET),
             List.of(Port.NDB_CLIENT),
             List.of(Network.NDB),
             List.of("ME_CONFIG_MONGODB_ENABLE_ADMIN=true",
                     "ME_CONFIG_OPTIONS_EDITORTHEME=rubyblue",
                     "ME_CONFIG_MONGODB_SERVER=" + Middleware.NDB,
                     "ME_CONFIG_MONGODB_PORT=" + Port.NDB.exposed,
                     "ME_CONFIG_MONGODB_AUTH_DATABASE=" + Credentials.DB_NAME,
                     "ME_CONFIG_MONGODB_AUTH_USERNAME=" + Credentials.DB_USER,
                     "ME_CONFIG_MONGODB_AUTH_PASSWORD=" + Credentials.DB_PASS,
                     "ME_CONFIG_MONGODB_ADMINUSERNAME=" + Credentials.DB_USER,
                     "ME_CONFIG_MONGODB_ADMINPASSWORD=" + Credentials.DB_PASS),
             List.of()),
  MSG("rabbitmq:management",
      List.of(),
      ServiceMode.GLOBAL,
      List.of(Mounts.SOCKET, Mounts.MSG),
      List.of(Port.MSG, Port.MSG_CLIENT),
      List.of(Network.MSG),
      List.of("RABBITMQ_DEFAULT_USER=" + Credentials.DB_USER,
              "RABBITMQ_DEFAULT_PASS=" + Credentials.DB_PASS),
      List.of()),
  AGENT("portainer/portainer-ce",
        List.of(),
        ServiceMode.GLOBAL,
        List.of(Mounts.SOCKET, Mounts.AGENT),
        List.of(Port.AGENT),
        List.of(),
        List.of(),
        List.of()),
  SCRAPPER("prom/prometheus",
           List.of(),
           ServiceMode.REPLICA,
           List.of(),
           List.of(Port.SCRAPPER),
           List.of(Network.SCRAPPER),
           List.of(),
           List.of()),
  SCRAPPER_RDB("prom/mysqld-exporter",
               List.of(SCRAPPER, RDB),
               ServiceMode.GLOBAL,
               List.of(),
               List.of(Port.SCRAPPER_RDB),
               List.of(Network.RDB, Network.SCRAPPER),
               List.of("DATA_SOURCE_NAME="
                           + Credentials.DB_USER + ":" + Credentials.DB_PASS
                           + "@(" + RDB + ":" + Port.RDB.published + ")"),
               List.of("/bin/mysqld_exporter",
                       "--collect.info_schema.processlist",
                       "--collect.info_schema.innodb_metrics",
                       "--collect.info_schema.tablestats",
                       "--collect.info_schema.tables",
                       "--collect.info_schema.userstats",
                       "--collect.engine_innodb_status",
                       "--web.listen-address=:" + Port.RDB.published)),
  //  MONITOR("grafana/grafana",
  //          List.of(SCRAPPER),
  //          ServiceMode.GLOBAL,
  //          List.of(),
  //          List.of(Port.MONITOR),
  //          List.of(Network.MONITOR),
  //          List.of("GF_SECURITY_ADMIN_USER=" + Credentials.DB_USER,
  //                  "GF_SECURITY_ADMIN_PASSWORD=" + Credentials.DB_PASS),
  //          List.of()),
  ;
  private final String image;
  private final List<Middleware> dependOn;
  private final ServiceMode mode;
  private final List<Mounts> mounts;
  private final List<Port> ports;
  private final List<Network> refs;
  private final List<String> entries;
  private final List<String> commands;

  /**
   * Extracts middleware profile items from a string input.
   *
   * @param ss comma-separated {@link Middleware} profiles input by
   *           {@link Main#main}.
   * @return Stream of Middleware inputs to be finished on.
   */
  static Stream<Middleware> stream(final String ss) {
    return Stream.of(ss.split(",")).map(Middleware::valueOf).distinct();
  }

  ServiceSpec spec() {
    val pp = ports.stream().map(Supplier::get).collect(Collectors.toList());
    val mm = mounts.stream().map(Supplier::get).collect(Collectors.toList());
    val u = new UpdateConfig()
        .withFailureAction(UpdateFailureAction.ROLLBACK);
    val n = refs.stream().map(Enum::name)
                .map(new NetworkAttachmentConfig()::withTarget)
                .collect(Collectors.toList());
    val c = new ContainerSpec().withMounts(mm).withImage(image)
                               .withEnv(entries).withCommand(commands);
    val t = new TaskSpec().withContainerSpec(c);
    val e = new EndpointSpec().withPorts(pp);
    return new ServiceSpec().withName(name()).withTaskTemplate(t)
                            .withNetworks(n).withEndpointSpec(e)
                            .withMode(mode.mode()).withRollbackConfig(u);
  }

  final List<Middleware> dependOn() {
    return dependOn;
  }

  @lombok.AllArgsConstructor
  private enum Port implements Supplier<PortConfig> {
    RDB(9011, 5432),
    NDB(9012, 27017),
    MSG(9013, 5672),
    // Clients
    AGENT(9000, 9000),
    MONITOR(9001, 3000),
    RDB_CLIENT(9003, 8080),
    NDB_CLIENT(9004, 8081),
    MSG_CLIENT(9005, 15672),
    // Monitoring
    SCRAPPER(9021, 9090),
    SCRAPPER_RDB(9022, 9104),
    ;
    private final int published;
    private final int exposed;

    @Override
    public PortConfig get() {
      return new PortConfig().withTargetPort(exposed)
                             .withPublishedPort(published)
                             .withPublishMode(PublishMode.host);
    }
  }

  private enum Network {
    RDB, NDB, MSG, SCRAPPER
  }

  @lombok.AllArgsConstructor
  private enum Mounts implements Supplier<Mount> {
    MSG("/var/lib/rabbitmq"),
    RDB("/var/lib/postgresql/data"),
    NDB("/data/db"),
    NDB_CFG("/data/configdb"),
    AGENT("/data"),
    SOCKET("") {
      @Override
      public Mount get() {
        return new Mount().withSource(Client.SOCKET)
                          .withTarget(Client.SOCKET)
                          .withReadOnly(Boolean.TRUE);
      }
    },
    ;
    private final String val;

    @Override
    public Mount get() {
      return new Mount().withType(MountType.VOLUME)
                        .withSource(name()).withTarget(val);
    }
  }

  @lombok.AllArgsConstructor
  private enum ServiceMode {
    GLOBAL(0),
    REPLICA(1),
    ;
    private final int scale;

    ServiceModeConfig mode() {
      return scale == 0
          ? new ServiceModeConfig().withGlobal(new ServiceGlobalModeOptions())
          : new ServiceModeConfig().withReplicated(
          new ServiceReplicatedModeOptions().withReplicas(scale));
    }
  }

  @lombok.experimental.UtilityClass
  private static class Credentials {
    private static final String DB_NAME = "base";
    private static final String DB_USER = "user";
    private static final String DB_PASS = "pass";
  }
}
