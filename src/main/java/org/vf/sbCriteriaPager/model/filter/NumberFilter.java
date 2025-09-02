package org.vf.sbCriteriaPager.model.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.vf.sbCriteriaPager.common.FilterType;
import org.vf.sbCriteriaPager.common.ValueType;

import java.io.Serializable;

@Getter
@Setter
public class NumberFilter extends CriteriaFilter implements Serializable {
    private final Integer value;
    private final Integer minValue;
    private final Integer maxValue;

    @Builder
    public NumberFilter(FilterType pFilterType,
                        ValueType pValueType,
                        Integer pValueInt,
                        Integer pMinValue,
                        Integer pMaxValue){
        super(pFilterType, pValueType);
        value = pValueInt;
        minValue = pMinValue;
        maxValue = pMaxValue;
    }
}
