package org.vf.sbCriteriaPager.model;

import lombok.*;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageQueryResponse<T> implements Serializable {
    private int totalPages;
    private int pageSize;
    private int pageNumber;
    private long totalRecords;
    private List<T> data;


}
