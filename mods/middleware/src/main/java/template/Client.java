package template;

import static java.util.Objects.requireNonNull;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.model.LocalNodeState;
import com.github.dockerjava.api.model.PruneType;
import com.github.dockerjava.api.model.Service;
import com.github.dockerjava.api.model.ServiceSpec;
import com.github.dockerjava.api.model.SwarmSpec;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import java.net.InetAddress;
import java.net.URI;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.val;

/**
 * Client for handling Docker concerns under an automated way.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@lombok.extern.slf4j.Slf4j
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class Client {

  static final String SOCKET = "/var/run/docker.sock";
  //
  private static final ZerodepDockerHttpClient.Builder HTTP =
      new ZerodepDockerHttpClient.Builder();
  private static final DockerClientBuilder CLI  = DockerClientBuilder
      .getInstance();
  //
  private final boolean isDevMode;
  private final InetAddress localhost;
  private final DockerClient host;

  @lombok.SneakyThrows
  static Client host(final boolean isDevMode) {
    val localhost = InetAddress.getLocalHost();
    val h = HTTP.dockerHost(URI.create("unix://" + SOCKET)).build();
    val cli = CLI.withDockerHttpClient(h).build();
    return new Client(isDevMode, localhost, cli);
  }

  void swarmMode(final int amount) {
    if (isDevMode) {
      if (isSwarmOn()) {
        host.leaveSwarmCmd().withForceEnabled(Boolean.TRUE).exec();
        log.info("Swarm left.");
      }
      // Prune resources
      host.listVolumesCmd().exec().getVolumes().stream()
          .map(InspectVolumeResponse::getName)
          .filter(n -> !n.equals(Middleware.AGENT.name()))
          .map(host::removeVolumeCmd)
          .forEach(rm -> {
            rm.exec();
            log.info("Volume pruned: [{}]", rm.getName());
          });
      host.pruneCmd(PruneType.NETWORKS).exec();
      log.info("Networks pruned.");
    }
    if (!isSwarmOn()) {
      host.initializeSwarmCmd(new SwarmSpec()).exec();
      log.info("Swarm started.");
    }
  }

  final void servicesFrom(final @lombok.NonNull Stream<Middleware> services) {
    val running = host
        .listServicesCmd().exec().stream().map(Service::getSpec)
        .filter(Objects::nonNull).map(ServiceSpec::getName)
        .map(Middleware::valueOf)
        .collect(Collectors.toSet());
    services.map(m -> EnumSet.of(m, m.dependOn().toArray(Middleware[]::new)))
            .flatMap(Collection::stream)
            .distinct()
            .filter(f -> !running.contains(f))
            .forEach(m -> {
              log.info("Service [{}]:", m);
              if (!m.name().contains("CLIENT")) {
                if (m != Middleware.AGENT
                    && host.listNetworksCmd()
                           .withNameFilter(m.name()).exec().isEmpty()) {
                  host.createNetworkCmd()
                      .withName(m.name())
                      .withAttachable(Boolean.TRUE)
                      .withDriver("overlay").exec();
                  log.info("* Network created.");
                }
                if (host.listVolumesCmd()
                        .withFilter("name", List.of(m.name()))
                        .exec().getVolumes().isEmpty()) {
                  host.createVolumeCmd().withName(m.name()).exec();
                  log.info("* Volume created.");
                }
              }
              host.createServiceCmd(m.spec()).exec();
              log.info("* Service created.");
              requireNonNull(requireNonNull(
                  m.spec().getEndpointSpec()).getPorts())
                  .forEach(p -> log.info("Listening to: http://{}:{}",
                                         localhost.getHostAddress(),
                                         p.getPublishedPort()));
            });
  }

  private boolean isSwarmOn() {
    val swarm = host.infoCmd().exec().getSwarm();
    return null != swarm && LocalNodeState.ACTIVE == swarm.getLocalNodeState();
  }
}
