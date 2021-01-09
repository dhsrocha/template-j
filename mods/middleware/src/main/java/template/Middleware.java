package template;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.ContainerSpec;
import com.github.dockerjava.api.model.EndpointSpec;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.PortConfig;
import com.github.dockerjava.api.model.PortConfig.PublishMode;
import com.github.dockerjava.api.model.ServiceGlobalModeOptions;
import com.github.dockerjava.api.model.ServiceModeConfig;
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
  DOCKER_MANAGER {
    @Override
    ServiceSpec build(final DockerClient client) {
      @Cleanup val cmd = client.createVolumeCmd();
      val v = cmd.withName("portainer_data").exec();
      log.info("Volume created for {}: [{}]", name(), v.getName());
      val c = new ContainerSpec()
          .withImage("portainer/portainer-ce")
          .withMounts(List.of(Constants.MOUNT,
                              new Mount().withType(MountType.VOLUME)
                                         .withSource(v.getName())
                                         .withTarget("/data")));
      val t = new TaskSpec().withContainerSpec(c);
      val e = new EndpointSpec().withPorts(
          List.of(new PortConfig().withTargetPort(9000)
                                  .withPublishedPort(9000)
                                  .withPublishMode(PublishMode.host)));
      val globalOpt = new ServiceGlobalModeOptions();
      val rollback = new UpdateConfig()
          .withFailureAction(UpdateFailureAction.ROLLBACK);
      return new ServiceSpec()
          .withName("docker-manager")
          .withTaskTemplate(t)
          .withEndpointSpec(e)
          .withMode(new ServiceModeConfig().withGlobal(globalOpt))
          .withRollbackConfig(rollback);
    }
  },
  ;

  abstract ServiceSpec build(final DockerClient client);

  @UtilityClass
  static class Constants {
    static final String SOCKET = "/var/run/docker.sock";
    private static final Mount MOUNT = new Mount().withSource(SOCKET)
                                                  .withTarget(SOCKET)
                                                  .withReadOnly(Boolean.TRUE);
  }
}
