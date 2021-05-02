package template.base.contract;

import com.google.gson.Gson;
import io.javalin.http.Context;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.val;

enum Support {
  ;
  static final Gson MAPPER = new Gson();
  static final String FQ = "fq";
  static final String SKIP = "skip";
  static final String LIMIT = "limit";

  static <T> T objOf(final @NonNull String str, final @NonNull Class<T> ref) {
    val m = MAPPER.fromJson("{" + str + "}", Map.class);
    return MAPPER.fromJson(Support.MAPPER.toJson(m), ref);
  }

  static Optional<Integer> intOf(final @NonNull Context ctx, final String s) {
    return Optional.ofNullable(ctx.queryParam(s)).map(Integer::parseInt);
  }
}
