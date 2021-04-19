package template.base.contract;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.Handler;

/**
 * General module for application's controllers.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Controller {

  Gson MAPPER = new Gson();

  /**
   * Controller for a returning a specific object, preferably, a value object.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  interface Getter<T> extends Handler {

    @Override
    default void handle(final @lombok.NonNull Context ctx) {
      ctx.result(MAPPER.toJson(get()));
    }

    T get();
  }
}
