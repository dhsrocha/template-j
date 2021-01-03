package template;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import java.net.URI;
import java.util.Arrays;
import java.util.EnumMap;
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
    Arrays.stream(args)
        .map(s -> s.split("[=:]"))
        .filter(s -> s.length == 2)
        .forEach(ss -> props.put(Props.valueOf(ss[0]), ss[1]));
    // Client
    val cfg = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    val http = new ZerodepDockerHttpClient.Builder()
        .dockerHost(URI.create("unix:///var/run/docker.sock"))
        .build();
    Arrays.stream(Props.values())
        .forEach(p -> log.info("{}: [{}]", p, props.get(p)));
    val client = DockerClientImpl.getInstance(cfg, http);
    log.info("Docker auth status: [{}].", client.authCmd().exec().getStatus());
  }

  private enum Props {
    /**
     * Indicates if the build is going to work in a development environment.
     * Typed as {@link Boolean} and defaults to {@code false}.
     */
    DEV_MODE,
    /**
     * Indicates the pod's name. Typed as {@link String} and defaults to
     * project's root folder ({@code Path.get("").toAbsolutePath().toString()}).
     */
    POD_NAME
  }
}
