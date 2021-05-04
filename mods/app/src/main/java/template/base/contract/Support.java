package template.base.contract;

import com.google.gson.Gson;
import java.util.Map;
import lombok.NonNull;
import lombok.val;

enum Support {
  ;
  static final Gson MAPPER = new Gson();
  static final String FQ = "fq";

  static <T> T objOf(final @NonNull String str, final @NonNull Class<T> ref) {
    val m = MAPPER.fromJson("{" + str + "}", Map.class);
    return MAPPER.fromJson(Support.MAPPER.toJson(m), ref);
  }
}
