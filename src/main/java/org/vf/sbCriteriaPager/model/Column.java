package org.vf.sbCriteriaPager.model;

import lombok.*;
import org.vf.sbCriteriaPager.common.SortType;
import org.vf.sbCriteriaPager.common.ValueType;
import org.vf.sbCriteriaPager.model.filter.CriteriaFilter;

import java.io.Serializable;
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Column implements Serializable {
    private String path;
    private String alias;
    private ValueType valueType;
    private CriteriaFilter criteriaFilter;
    private SortType sortType;
    private Boolean isFiltered;
    private Boolean isSorted;

}
