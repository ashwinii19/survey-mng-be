package com.survey.mapper;

import com.survey.dto.external.ZingEmployeeDTO;
import com.survey.dto.external.ZingAttributeDTO;
import com.survey.entity.Employee;
import java.util.List;

public class ZingEmployeeMapper {

    // -------------------------------------------------------------
    // CREATE NEW EMPLOYEE
    // -------------------------------------------------------------
    public static Employee mapToNew(ZingEmployeeDTO dto) {

        return Employee.builder()
                .employeeId(dto.getEmployeeCode())
                .name(buildFullName(dto))
                .email(dto.getEmail())
                .position(extractPosition(dto))
                .status(dto.getEmployeeStatus())
                .joinDate(dto.getDateOfJoining())
                .mobileNo(dto.getMobileNo())
                .salutation(dto.getSalutation())
                .department(null)   // IMPORTANT → Set later
                .build();
    }


    // -------------------------------------------------------------
    // UPDATE EXISTING EMPLOYEE
    // -------------------------------------------------------------
    public static void mapToExisting(Employee emp, ZingEmployeeDTO dto) {

        emp.setEmployeeId(dto.getEmployeeCode());
        emp.setName(buildFullName(dto));
        emp.setEmail(dto.getEmail());
        emp.setPosition(extractPosition(dto));
        emp.setStatus(dto.getEmployeeStatus());
        emp.setJoinDate(dto.getDateOfJoining());
        emp.setMobileNo(dto.getMobileNo());
        emp.setSalutation(dto.getSalutation());
    }


    // -------------------------------------------------------------
    // BUILD FULL NAME (WITHOUT SALUTATION)
    // -------------------------------------------------------------
    public static String buildFullName(ZingEmployeeDTO dto) {

        String first = dto.getFirstName() != null ? dto.getFirstName().trim() : "";
        String last  = dto.getLastName()  != null ? dto.getLastName().trim()  : "";

        return (first + " " + last).trim();
    }


    // -------------------------------------------------------------
    // EXTRACT POSITION (Designation)
    // -------------------------------------------------------------
    public static String extractPosition(ZingEmployeeDTO dto) {

        if (dto.getAttributes() == null) return null;

        for (ZingAttributeDTO att : dto.getAttributes()) {
            if ("Designation".equalsIgnoreCase(att.getAttributeTypeCode())) {
                return att.getAttributeTypeUnitCode();
            }
        }

        return null;
    }


    // -------------------------------------------------------------
    // EXTRACT DEPARTMENT
    // LOGIC:
    // 1) Prefer Practice
    // 2) If missing, use BusinessUnit
    // -------------------------------------------------------------
    public static String extractDeptName(ZingEmployeeDTO dto) {

        if (dto.getAttributes() == null) return null;

        String practice = null;
        String bu = null;

        for (ZingAttributeDTO att : dto.getAttributes()) {

            if ("Practice".equalsIgnoreCase(att.getAttributeTypeCode())) {
                practice = att.getAttributeTypeUnitDescription(); // GOOD NAME
            }

            if ("BusinessUnit".equalsIgnoreCase(att.getAttributeTypeCode())) {
                bu = att.getAttributeTypeUnitDescription();
            }
        }

        if (practice != null && !practice.isBlank()) return practice;
        if (bu != null && !bu.isBlank()) return bu;

        return null;
    }
}
