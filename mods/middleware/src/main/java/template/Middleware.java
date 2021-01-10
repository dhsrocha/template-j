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
import com.github.dockerjava.api.model.ServicePlacement;
import com.github.dockerjava.api.model.ServiceSpec;
import com.github.dockerjava.api.model.TaskSpec;
import com.github.dockerjava.api.model.UpdateConfig;
import com.github.dockerjava.api.model.UpdateFailureAction;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.val;

enum Middleware {
  RDS {
    @Override
    ServiceSpec spec() {
      // Build
      val c = new ContainerSpec()
          .withImage("postgres")
          .withMounts(List.of(Middleware.MOUNT,
                              self().volumeOf("/var/lib/postgresql/data")))
          .withEnv(List.of("POSTGRES_DB=5432",
                           "POSTGRES_DB=" + Credentials.DB_NAME,
                           "POSTGRES_USER=" + Credentials.DB_USER,
                           "POSTGRES_PASSWORD=" + Credentials.DB_PASS));
      val e = new EndpointSpec()
          .withPorts(List.of(self().portOf(5432, 5432)));
      val t = new TaskSpec()
          .withContainerSpec(c)
          .withPlacement(new ServicePlacement().withMaxReplicas(2));
      return self().specWith(t, e, this);
    }
  },
  RDS_CLIENT {
    @Override
    ServiceSpec spec() {
      val c = new ContainerSpec()
          .withImage("adminer")
          .withMounts(List.of(Middleware.MOUNT))
          .withEnv(List.of("ADMINER_DEFAULT_SERVER=" + RDS.name()));
      val e = new EndpointSpec()
          .withPorts(List.of(self().portOf(8080, 9001)));
      val t = new TaskSpec().withContainerSpec(c);
      return self().specWith(t, e, RDS);
    }
  },
  NOSQL {
    @Override
    ServiceSpec spec() {
      val c = new ContainerSpec()
          .withImage("mongo")
          .withMounts(List.of(Middleware.MOUNT,
                              self().volumeOf("/data/db"),
                              self().volumeOf("/data/configdb")))
          .withEnv(List.of("POSTGRES_DB=5432",
                           "MONGO_INITDB_ROOT_USERNAME=" + Credentials.DB_USER,
                           "MONGO_INITDB_ROOT_PASSWORD=" + Credentials.DB_PASS));
      val e = new EndpointSpec()
          .withPorts(List.of(self().portOf(27001, 27001)));
      val t = new TaskSpec().withContainerSpec(c);
      return self().specWith(t, e, this);
    }
  },
  NOSQL_CLIENT {
    @Override
    ServiceSpec spec() {
      val c = new ContainerSpec()
          .withImage("mongo-express")
          .withMounts(List.of(Middleware.MOUNT))
          .withEnv(List.of("ME_CONFIG_MONGODB_ENABLE_ADMIN=true",
                           "ME_CONFIG_OPTIONS_EDITORTHEME=rubyblue",
                           "ME_CONFIG_MONGODB_SERVER="
                               + Middleware.NOSQL.name(),
                           "ME_CONFIG_MONGODB_AUTH_DATABASE=" + Credentials.DB_NAME,
                           "ME_CONFIG_MONGODB_AUTH_USERNAME=" + Credentials.DB_USER,
                           "ME_CONFIG_MONGODB_AUTH_PASSWORD=" + Credentials.DB_PASS,
                           "ME_CONFIG_MONGODB_ADMINUSERNAME=" + Credentials.DB_USER,
                           "ME_CONFIG_MONGODB_ADMINPASSWORD=" + Credentials.DB_PASS));
      val e = new EndpointSpec()
          .withPorts(List.of(self().portOf(8081, 9002)));
      val t = new TaskSpec().withContainerSpec(c);
      return self().specWith(t, e, NOSQL);
    }
  },
  MSG {
    @Override
    ServiceSpec spec() {
      val c = new ContainerSpec()
          .withImage("rabbitmq:management")
          .withMounts(List.of(Middleware.MOUNT,
                              self().volumeOf("/var/lib/rabbitmq")))
          .withEnv(List.of("RABBITMQ_DEFAULT_USER=" + Credentials.DB_USER,
                           "RABBITMQ_DEFAULT_PASS=" + Credentials.DB_PASS));
      val e = new EndpointSpec()
          .withPorts(List.of(self().portOf(5672, 5672),
                             self().portOf(15672, 9003)));
      val t = new TaskSpec().withContainerSpec(c);
      return self().specWith(t, e, this);
    }
  },
  AGENT {
    @Override
    ServiceSpec spec() {
      val c = new ContainerSpec()
          .withImage("portainer/portainer-ce")
          .withMounts(List.of(Middleware.MOUNT,
                              self().volumeOf("/data")));
      val t = new TaskSpec().withContainerSpec(c);
      val e = new EndpointSpec()
          .withPorts(List.of(self().portOf(9000, 9000)));
      return self().specWith(t, e);
    }
  },
  ;

  private static final Mount MOUNT = new Mount().withSource(Constants.SOCKET)
                                                .withTarget(Constants.SOCKET)
                                                .withReadOnly(Boolean.TRUE);

  abstract ServiceSpec spec();

  Middleware self() {
    return this;
  }

  private ServiceSpec specWith(final TaskSpec task, final EndpointSpec endpoint,
                               final Middleware... networksRefs) {
    val u = new UpdateConfig()
        .withFailureAction(UpdateFailureAction.ROLLBACK);
    val s = new ServiceModeConfig()
        .withGlobal(new ServiceGlobalModeOptions());
    val nn = Arrays.stream(networksRefs).map(Enum::name)
                   .map(new NetworkAttachmentConfig()::withTarget)
                   .collect(Collectors.toList());
    return new ServiceSpec().withName(name())
                            .withTaskTemplate(task)
                            .withEndpointSpec(endpoint)
                            .withRollbackConfig(u)
                            .withMode(s)
                            .withNetworks(nn);
  }

  private Mount volumeOf(final String target) {
    return new Mount().withSource(name()).withTarget(target)
                      .withType(MountType.VOLUME);
  }

  private PortConfig portOf(final int source, final int target) {
    return new PortConfig().withTargetPort(source)
                           .withPublishedPort(target)
                           .withPublishMode(PublishMode.host);
  }

  @UtilityClass
  static class Constants {
    static final String SOCKET = "/var/run/docker.sock";
  }

  @UtilityClass
  private static class Credentials {
    private static final String DB_NAME = "base";
    private static final String DB_USER = "user";
    private static final String DB_PASS = "pass";
  }
}
