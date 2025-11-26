//package com.survey.batch;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//import java.util.List;
//import java.util.Locale;
//
//import org.springframework.batch.item.file.mapping.FieldSetMapper;
//import org.springframework.batch.item.file.transform.FieldSet;
//import org.springframework.validation.BindException;
//
//import com.survey.dto.request.EmployeeRequestDTO;
//
//public class EmployeeFieldSetMapper implements FieldSetMapper<EmployeeRequestDTO> {
//
//    private static final List<DateTimeFormatter> SUPPORTED_FORMATS = List.of(
//            DateTimeFormatter.ofPattern("M/d/yyyy", Locale.ENGLISH),
//            DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH),
//            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
//            DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
//    );
//
//    @Override
//    public EmployeeRequestDTO mapFieldSet(FieldSet fieldSet) throws BindException {
//
//        // 🟡 Skip empty rows
//        if (fieldSet == null || fieldSet.readString(0).trim().isEmpty()) {
//            return null;
//        }
//
//        String dateStr = fieldSet.readString("joinDate");
//        String departmentIdStr = fieldSet.readString("departmentId");
//
//        LocalDate joinDate = parseFlexibleDate(dateStr);
//
//        Long departmentId = null;
//        if (departmentIdStr != null && !departmentIdStr.isEmpty()) {
//            try {
//                departmentId = Long.parseLong(departmentIdStr);
//            } catch (NumberFormatException e) {
//                System.out.println("⚠️ Invalid departmentId format: " + departmentIdStr);
//            }
//        }
//
//        EmployeeRequestDTO dto = new EmployeeRequestDTO();
//        dto.setEmployeeId(fieldSet.readString("employeeId"));
//        dto.setName(fieldSet.readString("name"));
//        dto.setEmail(fieldSet.readString("email"));
//        dto.setPosition(fieldSet.readString("position"));
//        dto.setStatus(fieldSet.readString("status"));
//        dto.setJoinDate(joinDate != null ? joinDate.toString() : null);
//        dto.setDepartmentId(departmentId);
//
//        return dto;
//    }
//
//    private LocalDate parseFlexibleDate(String dateStr) {
//        if (dateStr == null || dateStr.trim().isEmpty()) {
//            return null;
//        }
//        for (DateTimeFormatter formatter : SUPPORTED_FORMATS) {
//            try {
//                return LocalDate.parse(dateStr.trim(), formatter);
//            } catch (DateTimeParseException ignored) {}
//        }
//        System.out.println("⚠️ Unrecognized date format: " + dateStr);
//        return null;
//    }
//}





package com.survey.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.survey.dto.request.EmployeeRequestDTO;

public class EmployeeFieldSetMapper implements FieldSetMapper<EmployeeRequestDTO> {

    private static final List<DateTimeFormatter> SUPPORTED_FORMATS = List.of(
            DateTimeFormatter.ofPattern("M/d/yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
    );

    @Override
    public EmployeeRequestDTO mapFieldSet(FieldSet fieldSet) throws BindException {

        // 🟡 Skip empty rows
        if (fieldSet == null || fieldSet.readString(0).trim().isEmpty()) {
            return null;
        }

        String dateStr = fieldSet.readString("joinDate");
        // 🟡 CHANGE: Read departmentName instead of departmentId
        String departmentName = fieldSet.readString("departmentName");

        LocalDate joinDate = parseFlexibleDate(dateStr);

        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setEmployeeId(fieldSet.readString("employeeId"));
        dto.setName(fieldSet.readString("name"));
        dto.setEmail(fieldSet.readString("email"));
        dto.setPosition(fieldSet.readString("position"));
        dto.setStatus(fieldSet.readString("status"));
        dto.setJoinDate(joinDate != null ? joinDate.toString() : null);
        // 🟡 CHANGE: Set departmentName instead of departmentId
        dto.setDepartmentName(departmentName);

        // 🟡 ADD LOGGING TO SEE WHAT'S BEING PARSED
        System.out.println("🟡 FIELD MAPPER: Parsed - " + 
                         dto.getEmployeeId() + ", " + 
                         dto.getEmail() + ", " + 
                         "Dept: '" + departmentName + "'");

        return dto;
    }

    private LocalDate parseFlexibleDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        for (DateTimeFormatter formatter : SUPPORTED_FORMATS) {
            try {
                return LocalDate.parse(dateStr.trim(), formatter);
            } catch (DateTimeParseException ignored) {}
        }
        System.out.println("⚠️ Unrecognized date format: " + dateStr);
        return null;
    }
}