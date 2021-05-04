package template.base.contract;

import io.javalin.http.Handler;
import template.base.stereotype.Referable;

/**
 * Contract for filters.
 *
 * @param <T>
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Filter<T> extends Referable<T> {

  Handler filter();
}
