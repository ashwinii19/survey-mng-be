package com.survey.mapper;

import com.survey.dto.external.ZingAttributeDTO;
import com.survey.dto.external.ZingEmployeeDTO;
import com.survey.entity.Department;
import com.survey.entity.Employee;

public class ZingEmployeeMapper {

    public static Employee map(ZingEmployeeDTO dto) {

        Employee emp = new Employee();

        emp.setEmployeeId(dto.getEmployeeCode());

        String fullName = (dto.getSalutation() + " " +
                dto.getFirstName() + " " +
                dto.getLastName()).trim();

        emp.setName(fullName);

        emp.setEmail(dto.getEmail());
        emp.setJoinDate(dto.getDateOfJoining());
        emp.setStatus(dto.getEmployeeStatus());

        if (dto.getAttributes() != null) {
            for (ZingAttributeDTO attr : dto.getAttributes()) {

                if ("Department".equalsIgnoreCase(attr.getAttributeTypeCode())) {

                    Department dept = new Department();
                    dept.setName(attr.getAttributeTypeUnitCode());
                    emp.setDepartment(dept);
                    break;
                }
            }
        }

        return emp;
    }
}
