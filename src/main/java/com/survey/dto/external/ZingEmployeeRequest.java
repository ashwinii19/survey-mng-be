package com.survey.dto.external;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZingEmployeeRequest {

    private String FromDate;
    private String ToDate;
    private String DateFilterOn;  // MUST be "DOJ" or "DOL"
    private int PageSize;
    private int PageNumber;
}
