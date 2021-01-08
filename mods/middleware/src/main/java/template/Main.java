package template;

import com.github.dockerjava.api.model.ContainerSpec;
import com.github.dockerjava.api.model.EndpointSpec;
import com.github.dockerjava.api.model.LocalNodeState;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.PortConfig;
import com.github.dockerjava.api.model.PortConfig.PublishMode;
import com.github.dockerjava.api.model.Service;
import com.github.dockerjava.api.model.ServiceGlobalModeOptions;
import com.github.dockerjava.api.model.ServiceModeConfig;
import com.github.dockerjava.api.model.ServiceSpec;
import com.github.dockerjava.api.model.SwarmSpec;
import com.github.dockerjava.api.model.TaskSpec;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import java.net.URI;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@UtilityClass
public class Main {

  private static final String SOCKET = "/var/run/docker.sock";

  /**
   * Builds middleware assets according to a given environment.
   *
   * @param args Key-value arguments written in {@code k=v} or {@code k:v} under
   *             {@link Props pre-defined keys}. Any string that does not follow
   *             that pattern is going to be discarded.
   */
  public static void main(final String[] args) {
    Props.parse(args);
    // Client
    val cfg = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    val http = new ZerodepDockerHttpClient.Builder()
        .dockerHost(URI.create("unix://" + SOCKET))
        .build();
    val client = DockerClientImpl.getInstance(cfg, http);
    Stream.of(Props.values()).forEach(p -> log
        .info("Property {}: [{}]", p, p.getAs(String::valueOf)));
    // Swarm / Stack
    val isDev = Props.DEV_MODE.getAs(Boolean::parseBoolean);
    if (isDev) {
      val swarm = client.infoCmd().exec().getSwarm();
      if (null != swarm && swarm.getLocalNodeState() == LocalNodeState.ACTIVE) {
        client.leaveSwarmCmd().withForceEnabled(Boolean.TRUE).exec();
        log.info("Swarm left.");
      }
      client.initializeSwarmCmd(new SwarmSpec()).exec();
      log.info("Swarm joined.");
    }
    val vol = client.createVolumeCmd().withName("portainer_data").exec();
    log.info("Volume created: [{}]", vol.getName());
    val container = new ContainerSpec()
        .withImage("portainer/portainer-ce")
        .withMounts(List.of(new Mount().withSource(SOCKET)
                                       .withTarget(SOCKET),
                            new Mount().withType(MountType.VOLUME)
                                       .withSource(vol.getName())
                                       .withTarget("/data")));
    val task = new TaskSpec().withContainerSpec(container);
    val endpoint = new EndpointSpec().withPorts(
        List.of(new PortConfig().withTargetPort(9000)
                                .withPublishedPort(9000)
                                .withPublishMode(PublishMode.host)));
    val svc = new ServiceSpec()
        .withName("docker-manager")
        .withTaskTemplate(task)
        .withEndpointSpec(endpoint)
        .withMode(new ServiceModeConfig()
                      .withGlobal(new ServiceGlobalModeOptions()));
    val id = client.createServiceCmd(svc).exec().getId();
    log.info("Services created:");
    client.listServicesCmd()
          .withIdFilter(List.of(id)).exec().stream()
          .map(Service::getSpec)
          .filter(Objects::nonNull)
          .map(ServiceSpec::getName)
          .forEach(log::info);
    log.info("Docker auth status: [{}].", client.authCmd().exec().getStatus());
  }

  @AllArgsConstructor
  private enum Props {
    /**
     * Indicates if the build is going to work in a development environment.
     * Defaults to {@code false}.
     */
    DEV_MODE("false"),
    /**
     * Indicates the pod's name. Defaults to project's root folder.
     */
    POD_NAME(Paths.get("").toAbsolutePath().getFileName().toString()),
    ;
    private static final Map<Props, String> MAP = new EnumMap<>(Props.class);
    private final String fallback;

    static void parse(final String... args) {
      Stream.of(args)
            .map(s -> s.split("[=:]"))
            .filter(s -> s.length == 2)
            .forEach(ss -> MAP.put(Props.valueOf(ss[0]), ss[1]));
    }

    final <T> T getAs(final Function<String, T> fun) {
      return fun.apply(MAP.getOrDefault(this, fallback));
    }
  }
}
