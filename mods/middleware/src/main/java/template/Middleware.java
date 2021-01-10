package template;

import com.github.dockerjava.api.DockerClient;
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
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
enum Middleware {
  RDS {
    @Override
    ServiceSpec build(final DockerClient client) {
      // Resources
      @Cleanup val volCmd = client.createVolumeCmd();
      val v = volCmd.withName(name() + "_data").exec();
      log.info("Volume created: [{}]", v.getName());
      val n = client.createNetworkCmd().withName(name())
                    .withAttachable(Boolean.TRUE).withDriver("overlay").exec();
      log.info("Network created: [{}]", n.getId());
      // Build
      val mount = "/var/lib/postgresql/data";
      val c = new ContainerSpec()
          .withImage("postgres")
          .withMounts(List.of(Config.MOUNT,
                              new Mount().withType(MountType.VOLUME)
                                         .withSource(v.getName())
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
          .withNetworks(List.of(Config.NETWORK_RDS));
    }
  },
  DB_CLIENT {
    @Override
    ServiceSpec build(final DockerClient client) {
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
          .withNetworks(List.of(Config.NETWORK_RDS));
    }
  },
  DOCKER_MANAGER {
    @Override
    ServiceSpec build(final DockerClient client) {
      @Cleanup val cmd = client.createVolumeCmd();
      val v = cmd.withName("portainer_data").exec();
      log.info("Volume created for {}: [{}]", name(), v.getName());
      val c = new ContainerSpec()
          .withImage("portainer/portainer-ce")
          .withMounts(List.of(Config.MOUNT,
                              new Mount().withType(MountType.VOLUME)
                                         .withSource(v.getName())
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

  abstract ServiceSpec build(final DockerClient client);

  @UtilityClass
  static class Constants {
    static final String SOCKET = "/var/run/docker.sock";
  }

  @UtilityClass
  private static class Config {

    private static final NetworkAttachmentConfig NETWORK_RDS =
        new NetworkAttachmentConfig().withTarget(Middleware.RDS.name());

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
