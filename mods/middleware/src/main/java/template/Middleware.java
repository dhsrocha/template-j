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
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.val;

enum Middleware {
  RDS {
    @Override
    ServiceSpec spec() {
      // Build
      val mount = "/var/lib/postgresql/data";
      val c = new ContainerSpec()
          .withImage("postgres")
          .withMounts(List.of(Config.MOUNT,
                              new Mount().withType(MountType.VOLUME)
                                         .withSource(name())
                                         .withTarget(mount)))
          .withEnv(List.of("POSTGRES_DB=5432",
                           "POSTGRES_DB=" + Credentials.DB_NAME,
                           "POSTGRES_USER=" + Credentials.DB_USER,
                           "POSTGRES_PASSWORD=" + Credentials.DB_PASS));
      val e = new EndpointSpec().withPorts(
          List.of(new PortConfig().withTargetPort(5432)
                                  .withPublishedPort(5432)
                                  .withPublishMode(PublishMode.host)));
      val t = new TaskSpec()
          .withContainerSpec(c)
          .withPlacement(new ServicePlacement().withMaxReplicas(2));
      return new ServiceSpec()
          .withName(name())
          .withTaskTemplate(t)
          .withEndpointSpec(e)
          .withMode(Config.GLOBAL)
          .withUpdateConfig(Config.ROLLBACK)
          .withNetworks(List.of(Config.NETWORK_DB));
    }
  },
  RDS_CLIENT {
    @Override
    ServiceSpec spec() {
      val c = new ContainerSpec()
          .withImage("adminer")
          .withMounts(List.of(Config.MOUNT))
          .withEnv(List.of("ADMINER_DESIGN=pepa-linha",
                           "ADMINER_DEFAULT_SERVER=" + RDS.name()));
      val e = new EndpointSpec().withPorts(
          List.of(new PortConfig().withTargetPort(8080)
                                  .withPublishedPort(9001)
                                  .withPublishMode(PublishMode.host)));
      val t = new TaskSpec().withContainerSpec(c);
      return new ServiceSpec()
          .withName(name())
          .withTaskTemplate(t)
          .withEndpointSpec(e)
          .withMode(Config.GLOBAL)
          .withUpdateConfig(Config.ROLLBACK)
          .withNetworks(List.of(Config.NETWORK_DB));
    }
  },
  NOSQL {
    @Override
    ServiceSpec spec() {
      val c = new ContainerSpec()
          .withImage("mongo")
          .withMounts(List.of(Config.MOUNT,
                              new Mount().withType(MountType.VOLUME)
                                         .withSource(name())
                                         .withTarget("/data/db"),
                              new Mount().withType(MountType.VOLUME)
                                         .withSource(name() + "_CONFIG")
                                         .withTarget("/data/configdb")))
          .withEnv(List.of("POSTGRES_DB=5432",
                           "MONGO_INITDB_ROOT_USERNAME=" + Credentials.DB_USER,
                           "MONGO_INITDB_ROOT_PASSWORD=" + Credentials.DB_PASS));
      val e = new EndpointSpec().withPorts(
          List.of(new PortConfig().withTargetPort(27001)
                                  .withPublishedPort(27001)
                                  .withPublishMode(PublishMode.host)));
      val t = new TaskSpec().withContainerSpec(c);
      return new ServiceSpec()
          .withName(name())
          .withTaskTemplate(t)
          .withEndpointSpec(e)
          .withMode(Config.GLOBAL)
          .withUpdateConfig(Config.ROLLBACK)
          .withNetworks(List.of(Config.NETWORK_DB));
    }
  },
  NOSQL_CLIENT {
    @Override
    ServiceSpec spec() {
      val c = new ContainerSpec()
          .withImage("mongo-express")
          .withMounts(List.of(Config.MOUNT))
          .withEnv(List.of("ME_CONFIG_MONGODB_ENABLE_ADMIN=true",
                           "ME_CONFIG_OPTIONS_EDITORTHEME=rubyblue",
                           "ME_CONFIG_MONGODB_SERVER="
                               + Middleware.NOSQL.name(),
                           "ME_CONFIG_MONGODB_AUTH_DATABASE=" + Credentials.DB_NAME,
                           "ME_CONFIG_MONGODB_AUTH_USERNAME=" + Credentials.DB_USER,
                           "ME_CONFIG_MONGODB_AUTH_PASSWORD=" + Credentials.DB_PASS,
                           "ME_CONFIG_MONGODB_ADMINUSERNAME=" + Credentials.DB_USER,
                           "ME_CONFIG_MONGODB_ADMINPASSWORD=" + Credentials.DB_PASS));
      val e = new EndpointSpec().withPorts(
          List.of(new PortConfig().withTargetPort(27018)
                                  .withPublishedPort(8081)
                                  .withPublishMode(PublishMode.host)));
      val t = new TaskSpec().withContainerSpec(c);
      return new ServiceSpec()
          .withName(name())
          .withTaskTemplate(t)
          .withEndpointSpec(e)
          .withMode(Config.GLOBAL)
          .withUpdateConfig(Config.ROLLBACK)
          .withNetworks(List.of(Config.NETWORK_DB));
    }
  },
  AGENT {
    @Override
    ServiceSpec spec() {
      val c = new ContainerSpec()
          .withImage("portainer/portainer-ce")
          .withMounts(List.of(Config.MOUNT,
                              new Mount().withType(MountType.VOLUME)
                                         .withSource(name())
                                         .withTarget("/data")));
      val t = new TaskSpec().withContainerSpec(c);
      val e = new EndpointSpec().withPorts(
          List.of(new PortConfig().withTargetPort(9000)
                                  .withPublishedPort(9000)
                                  .withPublishMode(PublishMode.host)));
      return new ServiceSpec()
          .withName(name())
          .withTaskTemplate(t)
          .withEndpointSpec(e)
          .withMode(Config.GLOBAL)
          .withRollbackConfig(Config.ROLLBACK);
    }
  },
  ;

  abstract ServiceSpec spec();

  enum Network {
    PRIVATE
  }

  @UtilityClass
  static class Constants {
    static final String SOCKET = "/var/run/docker.sock";
  }

  @UtilityClass
  private static class Config {

    private static final NetworkAttachmentConfig NETWORK_DB =
        new NetworkAttachmentConfig().withTarget(Network.PRIVATE.name());

    private static final Mount MOUNT = new Mount().withSource(Constants.SOCKET)
                                                  .withTarget(Constants.SOCKET)
                                                  .withReadOnly(Boolean.TRUE);

    private static final ServiceModeConfig GLOBAL = new ServiceModeConfig()
        .withGlobal(new ServiceGlobalModeOptions());

    private static final UpdateConfig ROLLBACK = new UpdateConfig()
        .withFailureAction(UpdateFailureAction.ROLLBACK);
  }

  @UtilityClass
  private static class Credentials {
    private static final String DB_NAME = "base";
    private static final String DB_USER = "user";
    private static final String DB_PASS = "pass";
  }
}
