package com.survey.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.survey.dto.response.SurveySubmissionResponseDTO;
import com.survey.entity.Department;
import com.survey.entity.Employee;
import com.survey.entity.Survey;
import com.survey.entity.SurveyResponse;
import com.survey.repository.EmployeeRepository;
import com.survey.repository.SurveyAssignmentRepository;
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

    @Override
    public List<SurveySubmissionResponseDTO> getAllSurveyResponsesSummary() {
        List<Survey> surveys = surveyRepository.findAll();
        return surveys.stream().map(this::buildSurveySummary).collect(Collectors.toList());
    }

    @Override
    public SurveySubmissionResponseDTO getSurveyResponseSummary(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));
        return buildSurveySummary(survey);
    }

    @Override
    public SurveySubmissionResponseDTO getSurveyResponseSummaryByDept(Long surveyId, Long departmentId) {
        return getFilteredSurveyResponses(surveyId, departmentId, null, null, null);
    }

    @Override
    public SurveySubmissionResponseDTO getFilteredSurveyResponses(Long surveyId,
                                                                  Long departmentId,
                                                                  String employeeName,
                                                                  LocalDate fromDate,
                                                                  LocalDate toDate) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        Department dept = (departmentId != null) ? survey.getTargetDepartment() : null;

        List<Employee> employees = (dept != null)
                ? employeeRepository.findByDepartmentId(departmentId)
                : employeeRepository.findAll();

        // Fetch all responses for survey (optionally by date)
        List<SurveyResponse> responses;
        if (fromDate != null && toDate != null) {
            responses = surveyResponseRepository.findBySurveyIdAndSubmittedAtBetween(
                    surveyId, fromDate.atStartOfDay(), toDate.atTime(23, 59));
        } else {
            responses = surveyResponseRepository.findBySurveyId(surveyId);
        }

        // Now filter responses manually by department
        List<String> deptEmployeeIds = employees.stream()
                .map(Employee::getEmployeeId)
                .collect(Collectors.toList());

        List<SurveyResponse> filteredResponses = responses.stream()
                .filter(r -> deptEmployeeIds.contains(r.getEmployeeId()))
                .collect(Collectors.toList());

        List<String> submitted = filteredResponses.stream()
                .map(SurveyResponse::getEmployeeId)
                .map(id -> employees.stream()
                        .filter(e -> e.getEmployeeId().equals(id))
                        .map(Employee::getName)
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .filter(name -> employeeName == null || name.toLowerCase().contains(employeeName.toLowerCase()))
                .collect(Collectors.toList());

        List<String> pending = employees.stream()
                .map(Employee::getName)
                .filter(name -> !submitted.contains(name))
                .filter(name -> employeeName == null || name.toLowerCase().contains(employeeName.toLowerCase()))
                .collect(Collectors.toList());

        SurveySubmissionResponseDTO dto = new SurveySubmissionResponseDTO();
        dto.setSurveyId(survey.getId());
        dto.setSurveyTitle(survey.getTitle());
        dto.setDepartmentName(dept != null ? dept.getName() : "All Departments");
        dto.setTotalEmployees(employees.size());
        dto.setSubmittedCount(submitted.size());
        dto.setPendingCount(pending.size());
        dto.setSubmittedEmployees(submitted);
        dto.setPendingEmployees(pending);
        return dto;
    }


    @Transactional
    public void submitSurvey(Long surveyId, String employeeId, List<com.survey.entity.QuestionResponse> answers) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        SurveyResponse response = SurveyResponse.builder()
                .survey(survey)
                .employeeId(employee.getEmployeeId())
                .submittedAt(LocalDateTime.now())
                .build();

        for (com.survey.entity.QuestionResponse answer : answers) {
            answer.setSurveyResponse(response);
        }

        response.setQuestionResponses(answers);
        surveyResponseRepository.save(response);

        employee.setSubmitted(true);
        employeeRepository.save(employee);

        // SurveyAssignment lookup now uses business employeeId - ensure repo method supports String
        assignmentRepository.findBySurveyIdAndEmployeeId(surveyId, employee.getEmployeeId())
                .ifPresent(assignment -> {
                    assignment.setReminderSent(true);
                    assignmentRepository.save(assignment);
                });
    }

    private SurveySubmissionResponseDTO buildSurveySummary(Survey survey) {
        Department dept = survey.getTargetDepartment();
        List<Employee> employees = dept != null
                ? employeeRepository.findByDepartmentId(dept.getId())
                : employeeRepository.findAll();

        List<SurveyResponse> responses = surveyResponseRepository.findBySurveyId(survey.getId());

        List<String> submitted = responses.stream()
                .map(r -> {
                    String businessEmployeeId = r.getEmployeeId();
                    if (businessEmployeeId == null) return null;
                    Optional<Employee> opt = employeeRepository.findByEmployeeId(businessEmployeeId);
                    return opt.map(Employee::getName).orElse(null);
                })
                .filter(name -> name != null)
                .collect(Collectors.toList());

        List<String> pending = employees.stream()
                .map(Employee::getName)
                .filter(name -> name != null && !submitted.contains(name))
                .collect(Collectors.toList());

        SurveySubmissionResponseDTO dto = new SurveySubmissionResponseDTO();
        dto.setSurveyId(survey.getId());
        dto.setSurveyTitle(survey.getTitle());
        dto.setDepartmentName(dept != null ? dept.getName() : "N/A");
        dto.setTotalEmployees(employees.size());
        dto.setSubmittedCount(submitted.size());
        dto.setPendingCount(pending.size());
        dto.setSubmittedEmployees(submitted);
        dto.setPendingEmployees(pending);
        return dto;
    }
}
