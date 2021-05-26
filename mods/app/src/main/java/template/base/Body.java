package template.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

/**
 * Body representation from application context with serialization utilities.
 *
 * @param <T> The type handled by this class.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Body<T> {

  private static final Pattern PATTERN = Pattern.compile("(?<=[a-z])[A-Z]");
  private static final Gson GSON = new GsonBuilder()
      .setFieldNamingStrategy(f -> underScoredUppercase(f.getName())).create();
  private static final TypeToken<Map<String, String>> MAP = new TypeToken<>() {
  };

  String raw;
  Class<T> ref;

  /**
   * Creates a structure holding the attributes from provided reference type.
   * Meant to be used as a request criteria to be handled down on the other
   * dependent layers.
   *
   * @param s   JSON body in string form. Expected ref be valid.
   * @param ref The type reference of provided string parameter serialization.
   * @param <T> The type handled by this class.
   * @return Instance to generate other structures, whether being a type or map.
   * @see #of(Object)
   * @see #of(Map, Class)
   */
  public static <T> Body<T> of(final @NonNull String s,
                               final @NonNull Class<T> ref) {
    final var m = Exceptions.ILLEGAL_ARGUMENT.trapIn(() -> Map
        .copyOf(GSON.fromJson(s, MAP.getType())).entrySet().stream()
        .map(e -> Map.entry(underScoredUppercase(e.getKey()), e.getValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    return new Body<>(m.toString(), ref);
  }

  /**
   * Creates a structure holding the attributes from provided reference type.
   * Meant to be used as a request criteria to be handled down on the other
   * dependent layers.
   *
   * @param content Content to serialize.
   * @param <T>     The type handled by this class.
   * @return Instance to generate other structures, whether being a type or map.
   * @see #of(Map, Class)
   * @see #of(String, Class)
   */
  @SuppressWarnings("unchecked")
  public static <T> Body<T> of(final @NonNull T content) {
    return new Body<>(GSON.toJson(content), (Class<T>) content.getClass());
  }

  /**
   * Creates a structure holding the attributes from provided reference type.
   * Meant to be used as a request criteria to be handled down on the other
   * dependent layers.
   *
   * @param map Map of parameters. Attributes from reference type are going to
   *            be considered. Other ones will be discarded.
   * @param ref The type reference of provided string parameter serialization.
   * @param <T> The type handled by this class.
   * @return Instance to generate other structures, whether being a type or map.
   * @see #of(Object)
   * @see #of(String, Class)
   */
  public static <T> Body<T> of(final @NonNull Map<String, Object> map,
                               final @NonNull Class<T> ref) {
    return new Body<>(GSON.toJson(map), ref);
  }

  /**
   * Generates an map of parameters out from from provided JSON body.
   *
   * @return Map of parameters.
   * @see #toType()
   */
  public final Map<String, String> toMap() {
    return GSON.fromJson(raw, MAP.getType());
  }

  /**
   * Generates a serialized type out from from provided JSON body.
   *
   * @return Serialized type.
   * @see #toMap()
   */
  public final T toType() {
    return GSON.fromJson(GSON.toJson(toMap()), ref);
  }

  private static String underScoredUppercase(final @NonNull Object o) {
    return PATTERN.matcher(o.toString()).replaceAll(m -> '_' + m.group())
                  .toUpperCase();
  }
}
