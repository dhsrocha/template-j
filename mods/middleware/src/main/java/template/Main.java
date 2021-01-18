package template;

import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.model.LocalNodeState;
import com.github.dockerjava.api.model.PruneType;
import com.github.dockerjava.api.model.Service;
import com.github.dockerjava.api.model.SwarmSpec;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.val;
import org.slf4j.LoggerFactory;

public interface Main {

  /**
   * Builds middleware assets according to a given environment.
   *
   * @param args Key-value arguments written in {@code k=v} or {@code k:v} under
   *             {@link Props pre-defined keys}. Any string that does not follow
   *             that pattern is going to be discarded.
   */
  static void main(final String[] args) {
    val log = LoggerFactory.getLogger(Main.class);
    Props.parse(args);
    // Client
    val cfg = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    val http = new ZerodepDockerHttpClient.Builder()
        .dockerHost(URI.create("unix://" + Middleware.Constants.SOCKET))
        .build();
    val client = DockerClientImpl.getInstance(cfg, http);
    log.info("Properties: {}", Props.MAP.toString());
    // Swarm / Stack
    val isDev = Props.DEV_MODE.getAs(Boolean::parseBoolean);
    val middlewareNames = Predicate.<String>isEqual("AGENT")
        .or(Predicate.isEqual("CLIENT")).negate();
    val swarm = client.infoCmd().exec().getSwarm();
    if (isDev && null != swarm && swarm
        .getLocalNodeState() == LocalNodeState.ACTIVE) {
      client.leaveSwarmCmd().withForceEnabled(Boolean.TRUE).exec();
      log.info("Swarm left.");
      // Prune
      client.listVolumesCmd().exec().getVolumes().stream()
            .map(InspectVolumeResponse::getName)
            .filter(middlewareNames)
            .forEach(s -> client.removeVolumeCmd(s).exec());
      log.info("Volumes pruned.");
      client.pruneCmd(PruneType.NETWORKS).exec();
      log.info("Networks pruned.");
      client.initializeSwarmCmd(new SwarmSpec()).exec();
      log.info("Swarm init.");
    }
    // Resources
    Stream.of(Middleware.values())
          .map(Enum::name)
          .filter(middlewareNames)
          .forEach(m -> client.createNetworkCmd()
                              .withName(m)
                              .withAttachable(Boolean.TRUE)
                              .withDriver("overlay").exec());
    log.info("Networks created.");
    Stream.of(Middleware.values())
          .filter(m -> !m.name().contains("CLIENT"))
          .forEach(m -> client.createVolumeCmd().withName(m.name()).exec());
    log.info("Volumes created.");
    // Middleware
    client.listServicesCmd()
          .withIdFilter(Arrays.stream(Middleware.values())
                              .map(Middleware::spec)
                              .map(client::createServiceCmd)
                              .map(c -> c.exec().getId())
                              .collect(Collectors.toList()))
          .exec().stream()
          .map(Service::getSpec)
          .filter(Objects::nonNull)
          .forEach(s -> log.info("Service created: [{}]", s.getName()));
    log.info("Docker auth status: [{}].", client.authCmd().exec().getStatus());
  }

  @AllArgsConstructor
  enum Props {
    /**
     * Indicates if the build is going to work in a development environment.
     * Defaults to {@code false}.
     */
    DEV_MODE("false"),
    /**
     * Indicates the pod's name. Defaults to project's root folder.
     */
    POD_NAME(Path.of("").toAbsolutePath().toFile().getName()),
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
