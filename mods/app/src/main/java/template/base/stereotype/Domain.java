package template.base.stereotype;

import static template.base.Exceptions.INVALID_DOMAIN;

import lombok.NonNull;
import template.base.contract.Validated;

public interface Domain<D extends Domain<D>> extends Validated,
                                                     Comparable<D> {

  static <D extends Domain<D>> D validate(final @NonNull D d) {
    INVALID_DOMAIN.throwIf(IllegalArgumentException::new, () -> !d.isValid());
    return d;
  }
}
