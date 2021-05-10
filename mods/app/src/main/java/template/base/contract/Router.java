package template.base.contract;

import io.javalin.apibuilder.EndpointGroup;
import template.base.stereotype.Referable;

/**
 * Maps application's routes. Main purpose is to isolate third party
 * dependency from the rest of the system.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Router extends EndpointGroup {

  /**
   * Allows the extending type to handle a path for routing purposes.
   *
   * @param <T> The reference to use in the path.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  interface Path<T> extends Referable<T> {

    /**
     * Path variable identity that is handled by {@link template.Application
     * current web container}.
     */
    String PATH_ID = "/:id";

    /**
     * Provides a standard path based on provided {@link #ref() type reference}.
     *
     * @return The path.
     * @see #ref()
     */
    default String path() {
      return ref().getSimpleName().toLowerCase() + PATH_ID;
    }
  }
}
