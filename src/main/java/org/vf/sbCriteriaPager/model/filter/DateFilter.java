package org.vf.sbCriteriaPager.model.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.vf.sbCriteriaPager.common.FilterType;
import org.vf.sbCriteriaPager.common.ValueType;

import java.io.Serializable;
import java.util.Date;
@Getter
@Setter
public class DateFilter extends CriteriaFilter implements Serializable {
    private final Date value;
    private final Date startDate;
    private final Date endDate;

    @Builder
    public DateFilter(FilterType pFilterType,
                      ValueType pValueType,
                      Date pValueDate,
                      Date pStartDate,
                      Date pEndDate) {
        super(pFilterType, pValueType);
        value = pValueDate;
        startDate = pStartDate;
        endDate = pEndDate;
    }
}
