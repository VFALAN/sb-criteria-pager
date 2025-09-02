package org.vf.sbCriteriaPager.model.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.vf.sbCriteriaPager.common.FilterType;
import org.vf.sbCriteriaPager.common.ValueType;

import java.io.Serializable;

@Setter
@Getter
public class StringFilter extends CriteriaFilter implements Serializable {
    private final String regex;
    private final String value;

    @Builder
    public StringFilter(FilterType pFilterType,
                        ValueType pValueType,

                        String pValueStr,
                        String pRegexStr) {
        super(pFilterType, pValueType);
        value = pValueStr;
        regex = pRegexStr;
    }
}
