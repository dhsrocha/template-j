package template;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import java.net.URI;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@UtilityClass
public class Main {

  /**
   * Builds middleware assets according to a given environment.
   *
   * @param args Key-value arguments written in {@code k=v} or {@code k:v} under
   *             {@link Props pre-defined keys}. Any string that does not follow
   *             that pattern is going to be discarded.
   */
  @SneakyThrows
  public static void main(final String[] args) {
    // Properties
    val props = new EnumMap<Props, String>(Props.class);
    Stream.of(args)
        .map(s -> s.split("[=:]"))
        .filter(s -> s.length == 2)
        .forEach(ss -> props.put(Props.valueOf(ss[0]), ss[1]));
    // Client
    val cfg = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    val http = new ZerodepDockerHttpClient.Builder()
        .dockerHost(URI.create("unix:///var/run/docker.sock"))
        .build();
    val client = DockerClientImpl.getInstance(cfg, http);
    props.entrySet().forEach(p -> log.info(p.toString()));
    Stream.of(Props.values()).forEach(p ->
        log.info("Property {}:[{}]", p, props.getOrDefault(p, p.fallback)));
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
     * Indicates the pod's name. Defaults to  project's root folder.
     */
    POD_NAME(Paths.get("").toAbsolutePath().getFileName().toString()),
    ;
    private final String fallback;
  }
}
