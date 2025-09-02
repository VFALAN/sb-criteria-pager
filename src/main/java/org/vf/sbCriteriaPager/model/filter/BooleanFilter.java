package org.vf.sbCriteriaPager.model.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.vf.sbCriteriaPager.common.FilterType;
import org.vf.sbCriteriaPager.common.ValueType;

import java.io.Serializable;

@Getter
@Setter
public class BooleanFilter extends CriteriaFilter implements Serializable {
    private final boolean value;

    @Builder
    public BooleanFilter(FilterType filterType,
                         ValueType valueType,
                         boolean pValue) {
        super(filterType, valueType);
        value = pValue;
    }
}
