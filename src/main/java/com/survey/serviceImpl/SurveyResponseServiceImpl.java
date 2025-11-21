package com.survey.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.survey.dto.response.SurveySubmissionResponseDTO;
import com.survey.entity.Department;
import com.survey.entity.Employee;
import com.survey.entity.Survey;
import com.survey.entity.SurveyDepartmentMap;
import com.survey.entity.SurveyResponse;
import com.survey.repository.DepartmentRepository;
import com.survey.repository.EmployeeRepository;
import com.survey.repository.SurveyAssignmentRepository;
import com.survey.repository.SurveyDepartmentMapRepository;
import com.survey.repository.SurveyRepository;
import com.survey.repository.SurveyResponseRepository;
import com.survey.service.SurveyResponseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyResponseServiceImpl implements SurveyResponseService {

    private final SurveyRepository surveyRepository;
    private final EmployeeRepository employeeRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final SurveyAssignmentRepository assignmentRepository;
    private final DepartmentRepository departmentRepository;
    private final SurveyDepartmentMapRepository surveyDepartmentMapRepository;

    // ====================================================================================
    // GET ALL SURVEYS SUMMARY
    // ====================================================================================
    @Override
    public List<SurveySubmissionResponseDTO> getAllSurveyResponsesSummary() {
        return surveyRepository.findAll()
                .stream()
                .map(this::buildSurveySummary)
                .collect(Collectors.toList());
    }

    // ====================================================================================
    // GET SUMMARY BY SURVEY ID
    // ====================================================================================
    @Override
    public SurveySubmissionResponseDTO getSurveyResponseSummary(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));
        return buildSurveySummary(survey);
    }

    // ====================================================================================
    // GET SUMMARY FOR SELECTED DEPARTMENT
    // ====================================================================================
    @Override
    public SurveySubmissionResponseDTO getSurveyResponseSummaryByDept(Long surveyId, Long departmentId) {
        return getFilteredSurveyResponses(surveyId, departmentId, null, null, null);
    }

    // ====================================================================================
    // ADVANCED FILTERS — MULTIPLE DEPARTMENT SUPPORT
    // ====================================================================================
    @Override
    public SurveySubmissionResponseDTO getFilteredSurveyResponses(
            Long surveyId,
            Long departmentId,
            String employeeName,
            LocalDate fromDate,
            LocalDate toDate) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        List<SurveyDepartmentMap> mappings = surveyDepartmentMapRepository.findBySurveyId(surveyId);
        long totalDeptCount = departmentRepository.count();

        boolean isAllDeptSurvey = (mappings.isEmpty() || mappings.size() == totalDeptCount);

        Department selectedDept = null;

        if (departmentId != null) {
            selectedDept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found"));

            if (!isAllDeptSurvey) {
                boolean allowed = mappings.stream()
                        .anyMatch(m -> m.getDepartment().getId().equals(departmentId));

                if (!allowed) {
                    throw new RuntimeException(
                            "Survey is NOT assigned to department: " + selectedDept.getName()
                    );
                }
            }
        }

        // ---------------- FETCH EMPLOYEES ----------------
        List<Employee> employees;

        if (selectedDept != null) {
            employees = employeeRepository.findByDepartmentId(selectedDept.getId());
        } else {
            employees = employeeRepository.findAll();
        }

        // ---------------- FETCH RESPONSES ----------------
        List<SurveyResponse> responses;

        if (fromDate != null && toDate != null) {
            responses = surveyResponseRepository.findBySurveyIdAndSubmittedAtBetween(
                    surveyId,
                    fromDate.atStartOfDay(),
                    toDate.atTime(23, 59)
            );
        } else {
            responses = surveyResponseRepository.findBySurveyId(surveyId);
        }

        Set<String> allowedEmpIds = employees.stream()
                .map(Employee::getEmployeeId)
                .collect(Collectors.toSet());

        List<SurveyResponse> filteredResponses = responses.stream()
                .filter(r -> allowedEmpIds.contains(r.getEmployeeId()))
                .collect(Collectors.toList());

        // -------- Submitted --------
        List<String> submitted = filteredResponses.stream()
                .map(SurveyResponse::getEmployeeId)
                .map(id -> employees.stream()
                        .filter(e -> e.getEmployeeId().equals(id))
                        .map(Employee::getName)
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .filter(name -> employeeName == null ||
                        name.toLowerCase().contains(employeeName.toLowerCase()))
                .collect(Collectors.toList());

        // -------- Pending --------
        List<String> pending = employees.stream()
                .map(Employee::getName)
                .filter(name -> !submitted.contains(name))
                .filter(name -> employeeName == null ||
                        name.toLowerCase().contains(employeeName.toLowerCase()))
                .collect(Collectors.toList());

        // ---------------- BUILD DTO ----------------
        SurveySubmissionResponseDTO dto = new SurveySubmissionResponseDTO();

        dto.setSurveyId(survey.getId());
        dto.setSurveyTitle(survey.getTitle());
        dto.setDepartmentName(selectedDept != null ? selectedDept.getName() : "ALL");

        dto.setTotalEmployees(employees.size());
        dto.setSubmittedCount(submitted.size());
        dto.setPendingCount(pending.size());
        dto.setSubmittedEmployees(submitted);
        dto.setPendingEmployees(pending);

        return dto;
    }

    // ====================================================================================
    // SUBMIT SURVEY
    // ====================================================================================
    @Transactional
    public void submitSurvey(Long surveyId, String employeeId, List<com.survey.entity.QuestionResponse> answers) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);
        response.setEmployeeId(employee.getEmployeeId());
        response.setSubmittedAt(LocalDateTime.now());

        answers.forEach(a -> a.setSurveyResponse(response));
        response.setQuestionResponses(answers);

        surveyResponseRepository.save(response);

        assignmentRepository.findBySurveyIdAndEmployeeId(surveyId, employee.getEmployeeId())
                .ifPresent(a -> {
                    a.setReminderSent(true);
                    assignmentRepository.save(a);
                });
    }

    // ====================================================================================
    // SUMMARY FOR ADMIN LIST PAGE
    // ====================================================================================
    private SurveySubmissionResponseDTO buildSurveySummary(Survey survey) {

        List<SurveyDepartmentMap> mappings = surveyDepartmentMapRepository.findBySurveyId(survey.getId());
        long totalDeptCount = departmentRepository.count();

        List<Department> depts;

        if (mappings.isEmpty() || mappings.size() == totalDeptCount) {
            depts = departmentRepository.findAll();
        } else {
            depts = mappings.stream()
                    .map(SurveyDepartmentMap::getDepartment)
                    .collect(Collectors.toList());
        }

        List<Employee> employees = new ArrayList<>();
        for (Department d : depts) {
            employees.addAll(employeeRepository.findByDepartmentId(d.getId()));
        }

        List<SurveyResponse> responses = surveyResponseRepository.findBySurveyId(survey.getId());

        List<String> submitted = responses.stream()
                .map(r -> employeeRepository.findByEmployeeId(r.getEmployeeId())
                        .map(Employee::getName).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<String> pending = employees.stream()
                .map(Employee::getName)
                .filter(name -> !submitted.contains(name))
                .collect(Collectors.toList());

        SurveySubmissionResponseDTO dto = new SurveySubmissionResponseDTO();

        dto.setSurveyId(survey.getId());
        dto.setSurveyTitle(survey.getTitle());

        dto.setDepartmentName(
                mappings.size() == totalDeptCount
                        ? "ALL"
                        : depts.stream().map(Department::getName).collect(Collectors.joining(", "))
        );

        dto.setTotalEmployees(employees.size());
        dto.setSubmittedCount(submitted.size());
        dto.setPendingCount(pending.size());
        dto.setSubmittedEmployees(submitted);
        dto.setPendingEmployees(pending);

        return dto;
    }
}
