package com.survey.dto.external;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZingEmployeeDTO {
    private String employeeCode;
    private Long employeeID;
    private String salutation;
    private String firstName;
    private String lastName;
    private String email;
    private String dateOfJoining;
    private String employeeStatus;
    private List<ZingAttributeDTO> attributes;
}
