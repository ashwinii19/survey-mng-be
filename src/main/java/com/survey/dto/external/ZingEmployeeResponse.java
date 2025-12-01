package com.survey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZingEmployeeResponse {
    private int code;
    private ZingEmployeeWrapper data;
}
