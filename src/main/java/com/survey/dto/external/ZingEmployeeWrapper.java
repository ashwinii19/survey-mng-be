package com.survey.dto.external;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZingEmployeeWrapper {
    private String employeesCount;
    private String totalEmployeeCount;
    private List<ZingEmployeeDTO> employees;
}
