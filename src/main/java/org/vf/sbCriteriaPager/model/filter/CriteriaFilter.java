package org.vf.sbCriteriaPager.model.filter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.vf.sbCriteriaPager.common.FilterType;
import org.vf.sbCriteriaPager.common.ValueType;
import org.vf.sbCriteriaPager.help.CriteriaFilterDeserializer;

import java.io.Serializable;

@Getter
@Setter
@JsonDeserialize(using = CriteriaFilterDeserializer.class)
@AllArgsConstructor
@NoArgsConstructor
public abstract class CriteriaFilter implements Serializable {
    @NotEmpty(message = "FilterType is required")
    private FilterType filterType;
    @NotEmpty(message = "Value is required")
    private ValueType valueType;


}
