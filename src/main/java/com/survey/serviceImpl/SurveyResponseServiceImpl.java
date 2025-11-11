package com.survey.serviceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.survey.dto.response.SurveySubmissionResponseDTO;
import com.survey.entity.Department;
import com.survey.entity.Employee;
import com.survey.entity.Survey;
import com.survey.entity.SurveyResponse;
import com.survey.repository.EmployeeRepository;
import com.survey.repository.SurveyRepository;
import com.survey.repository.SurveyResponseRepository;
import com.survey.service.SurveyResponseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SurveyResponseServiceImpl implements SurveyResponseService {

    private final SurveyRepository surveyRepository;
    private final EmployeeRepository employeeRepository;
    private final SurveyResponseRepository surveyResponseRepository;

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

        // Choose query dynamically based on filters
        List<SurveyResponse> responses;
        if (departmentId != null && fromDate != null && toDate != null) {
            responses = surveyResponseRepository.findBySurveyIdAndEmployeeDepartmentIdAndSubmittedAtBetween(
                    surveyId, departmentId, fromDate.atStartOfDay(), toDate.atTime(23, 59));
        } else if (fromDate != null && toDate != null) {
            responses = surveyResponseRepository.findBySurveyIdAndSubmittedAtBetween(
                    surveyId, fromDate.atStartOfDay(), toDate.atTime(23, 59));
        } else if (departmentId != null) {
            responses = surveyResponseRepository.findBySurveyIdAndEmployeeDepartmentId(surveyId, departmentId);
        } else {
            responses = surveyResponseRepository.findBySurveyId(surveyId);
        }

        // Safely collect employee names
        List<String> submitted = responses.stream()
                .map((SurveyResponse r) -> {
                    Employee e = r.getEmployee();
                    return e != null ? e.getName() : null;
                })
                .filter(name -> name != null && (employeeName == null || name.toLowerCase().contains(employeeName.toLowerCase())))
                .collect(Collectors.toList());

        List<String> pending = employees.stream()
                .map((Employee e) -> e.getName())
                .filter(name -> name != null && !submitted.contains(name))
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

    private SurveySubmissionResponseDTO buildSurveySummary(Survey survey) {
        Department dept = survey.getTargetDepartment();
        List<Employee> employees = dept != null
                ? employeeRepository.findByDepartmentId(dept.getId())
                : List.of();

        List<SurveyResponse> responses = surveyResponseRepository.findBySurveyId(survey.getId());

        List<String> submitted = responses.stream()
                .map((SurveyResponse r) -> {
                    Employee e = r.getEmployee();
                    return e != null ? e.getName() : null;
                })
                .filter(name -> name != null)
                .collect(Collectors.toList());

        List<String> pending = employees.stream()
                .map((Employee e) -> e.getName())
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
