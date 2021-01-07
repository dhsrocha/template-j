package template;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.RestartPolicy;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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
  @SneakyThrows
  public static void main(final String[] args) {
    Props.parse(args);
    // Client
    val cfg = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    val http = new ZerodepDockerHttpClient.Builder()
        .dockerHost(URI.create("unix://" + SOCKET))
        .build();
    val client = DockerClientImpl.getInstance(cfg, http);
    Stream.of(Props.values()).forEach(p -> log
        .info("Property {}: [{}]", p, p.get(String::valueOf)));
    // Prune
    val names = Stream.of(Service.values()).map(s -> s.contName)
                      .collect(Collectors.toList());
    val containers = client.listContainersCmd()
                           .withShowAll(Boolean.TRUE)
                           .withNameFilter(names)
                           .exec();
    containers.stream()
              .map(Container::getNames).map(Arrays::toString)
              .forEach(c -> log.info("Listed container: {}", c));
    if (Props.DEV_MODE.get(Boolean::valueOf) && containers.isEmpty()) {
      Service.DOCKER_MANAGER.create(client, HostConfig
          .newHostConfig()
          .withRestartPolicy(RestartPolicy.alwaysRestart())
          .withPortBindings(
              PortBinding.parse("9000:9000:9000"),
              PortBinding.parse("8000:8000:8000"))
          // Standalone mode volume (aka portainer_data) is created on the fly.
          .withBinds(Bind.parse(SOCKET + ":" + SOCKET)));
    }
    log.info("Docker auth status: [{}].", client.authCmd().exec().getStatus());
  }

  @AllArgsConstructor
  enum Service {
    DOCKER_MANAGER("portainer/portainer-ce", "docker-manager"),
    ;
    private final String imgName;
    private final String contName;

    void create(final DockerClient cli, final HostConfig cfg) {
      try (val created = cli.createContainerCmd(imgName)) {
        val id = created.withName(contName).withHostConfig(cfg).exec().getId();
        cli.startContainerCmd(id).exec();
        val i = cli.inspectContainerCmd(id).exec();
        log.info("Service [{}]: [{}]", i.getName(), i.getState().getStatus());
      }
    }
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

    final <T> T get(final Function<String, T> fun) {
      return fun.apply(MAP.getOrDefault(this, fallback));
    }
  }
}
