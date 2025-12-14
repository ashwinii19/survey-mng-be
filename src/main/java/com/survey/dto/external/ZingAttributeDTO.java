package com.survey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZingAttributeDTO {
    private int attributeTypeID;
    private String attributeTypeCode;
    private String attributeTypeDescription;

    private int attributeTypeUnitID;
    private String attributeTypeUnitCode;
    private String attributeTypeUnitDescription;

    private String effectiveDate;
}
