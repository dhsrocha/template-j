package template.base.stereotype;

import java.util.Map;

/**
 * Abstraction of a domain object which data is handled at persistence layer.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Entity<I, D> {

  Map<I, D> getStore();
}
