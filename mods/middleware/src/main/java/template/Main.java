package template;

import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.command.RemoveImageCmd;
import com.github.dockerjava.api.model.Image;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.val;
import org.slf4j.LoggerFactory;

public interface Main {

  /**
   * Builds middleware assets according to a given environment. Main purpose is
   * to create or refresh all required resources for running an application.
   *
   * @param args key-value entries treated by {@link Props#from(String...)}.
   */
  static void main(final String[] args) {
    val log = LoggerFactory.getLogger(Main.class);
    val props = Props.from(args);
    // Client
    val cfg = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    val http = new ZerodepDockerHttpClient.Builder()
        .dockerHost(URI.create("unix://" + Middleware.Constants.SOCKET))
        .build();
    val client = DockerClientImpl.getInstance(cfg, http);
    log.info("Properties: {}", props.toString());
    client.listImagesCmd().withDanglingFilter(Boolean.TRUE).exec().stream()
          .map(Image::getId).map(client::removeImageCmd)
          .map(RemoveImageCmd::exec)
          .forEach(v -> log.info("Dangling image removed."));
    // Swarm / Stack
    val swarm = client.infoCmd().exec().getSwarm();
    if (Boolean.parseBoolean(props.get(Props.DEV_MODE)) && null != swarm
        && swarm.getLocalNodeState() == LocalNodeState.ACTIVE) {
      client.leaveSwarmCmd().withForceEnabled(Boolean.TRUE).exec();
      log.info("Swarm left.");
      // Prune
      client.listVolumesCmd().exec().getVolumes().stream()
            .map(InspectVolumeResponse::getName)
            .filter(Middleware.Constants.FILTER)
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
          .filter(Middleware.Constants.FILTER)
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
    private static final Pattern SPLIT = Pattern.compile("[:=]");
    private final String val;

    /**
     * Serialize input entries according to the enumerated items.
     *
     * @param args Key-value entries written in {@code k=v} or {@code k:v} under
     *             {@link Props pre-defined keys}. Any string that does not
     *             follow that pattern is going to be discarded.
     * @return Map of properties with their corresponding captured or default
     *     values if not found.
     */
    static Map<Props, String> from(final String... args) {
      val m = new EnumMap<Props, String>(Props.class);
      for (val p : values()) {
        m.put(p, p.val);
      }
      for (val s : args) {
        val ss = SPLIT.split(s, -1);
        m.put(Props.valueOf(ss[0]), ss[1]);
      }
      return m;
    }
  }
}
