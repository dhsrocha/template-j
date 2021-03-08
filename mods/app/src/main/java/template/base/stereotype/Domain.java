package template.base.stereotype;

import template.base.contract.Validated;

public interface Domain<D extends Domain<D>> extends Validated,
                                                     Comparable<D> {
}
