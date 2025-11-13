package com.survey.serviceImpl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.survey.dto.dashboard.*;
import com.survey.entity.*;
import com.survey.repository.*;
import com.survey.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository responseRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public DashboardResponseDTO getDashboardData(Long surveyId, Long departmentId) {

        DashboardResponseDTO dto = new DashboardResponseDTO();

        long totalEmployees = employeeRepository.count();
        dto.setTotalEmployees(totalEmployees);
        dto.setTotalSurveys(surveyRepository.count());

        // --------------------------------------------------
        // DEFAULT (all surveys)
        // --------------------------------------------------
        long totalSubmitted = (surveyId == null)
                ? responseRepository.count()
                : responseRepository.countBySurveyId(surveyId);

        dto.setTotalSubmitted(totalSubmitted);
        dto.setTotalPending(totalEmployees - totalSubmitted);

        // --------------------------------------------------
        // WHO HAS SUBMITTED THIS SURVEY?
        // --------------------------------------------------
        Set<String> submittedEmployeeIds;

        if (surveyId == null) {
            submittedEmployeeIds = responseRepository.findAll()
                    .stream()
                    .map(SurveyResponse::getEmployeeId)
                    .collect(Collectors.toSet());

        } else {
            submittedEmployeeIds = responseRepository.findBySurveyId(surveyId)
                    .stream()
                    .map(SurveyResponse::getEmployeeId)
                    .collect(Collectors.toSet());
        }

        // --------------------------------------------------
        // DEPARTMENT STATS (for department charts)
        // --------------------------------------------------
        List<Department> departments =
                (departmentId != null)
                        ? List.of(departmentRepository.findById(departmentId).orElseThrow())
                        : departmentRepository.findAll();

        List<DepartmentStatsDTO> deptStats =
                departments.stream()
                        .map(dept -> mapDepartmentStats(dept, submittedEmployeeIds))
                        .collect(Collectors.toList());

        dto.setDepartmentStats(deptStats);

        // --------------------------------------------------
        // SURVEY STATS (FOR SURVEY BAR & DONUT CHART)
        // --------------------------------------------------
        if (surveyId != null) {
            dto.setSurveyStats(buildSurveyStats(surveyId));
        }

        return dto;
    }

    private SurveyStatsDTO buildSurveyStats(Long surveyId) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        SurveyStatsDTO s = new SurveyStatsDTO();
        s.setSurveyId(survey.getId());
        s.setSurveyTitle(survey.getTitle());

        long totalEmployees = employeeRepository.count();
        long submitted = responseRepository.countBySurveyId(surveyId);

        s.setTotalEmployees(totalEmployees);
        s.setTotalSubmitted(submitted);
        s.setTotalPending(totalEmployees - submitted);

        return s;
    }

    private DepartmentStatsDTO mapDepartmentStats(Department department, Set<String> submittedEmployeeIds) {
        DepartmentStatsDTO dto = new DepartmentStatsDTO();

        List<Employee> employees = department.getEmployees();

        dto.setDepartmentName(department.getName());
        dto.setTotalEmployees(employees.size());

        long submitted =
                employees.stream()
                        .filter(e -> submittedEmployeeIds.contains(e.getEmployeeId()))
                        .count();

        dto.setSubmitted(submitted);
        dto.setPending(dto.getTotalEmployees() - submitted);

        return dto;
    }
}
