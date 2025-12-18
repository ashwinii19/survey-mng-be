package com.survey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

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
    private String mobileNo;
    private String gender;

    private String dateOfBirth;
    private String dateOfJoining;   // DOJ
    private String dateOfLeaving;   // DOL

    private String employeeStatus;
    private String lastModified;
    private String empFlag;

    private List<ZingAttributeDTO> attributes;
}
