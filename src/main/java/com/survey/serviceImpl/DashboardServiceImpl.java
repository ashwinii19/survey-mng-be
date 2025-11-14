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

        // total submitted depends on survey filter
        long totalSubmitted = (surveyId == null)
                ? responseRepository.count()
                : responseRepository.countBySurveyId(surveyId);

        dto.setTotalSubmitted(totalSubmitted);
        dto.setTotalPending(Math.max(0, totalEmployees - totalSubmitted));

        // build submitted ids set (survey or all)
        Set<String> submittedEmployeeIds = (surveyId == null)
                ? responseRepository.findAll().stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet())
                : responseRepository.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());

        // departments list (filtered or all)
        List<Department> departments = (departmentId != null)
                ? List.of(departmentRepository.findById(departmentId).orElseThrow(() -> new RuntimeException("Department not found")))
                : departmentRepository.findAll();

        List<DepartmentStatsDTO> deptStats = departments.stream()
                .map(d -> mapDepartmentStats(d, submittedEmployeeIds))
                .collect(Collectors.toList());
        dto.setDepartmentStats(deptStats);

        // survey stats (if survey filter)
        if (surveyId != null) {
            dto.setSurveyStats(buildSurveyStats(surveyId));
        }

        return dto;
    }

    @Override
    public List<Survey> listSurveys() {
        return surveyRepository.findAll();
    }

    @Override
    public List<Department> listDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public List<DepartmentStatsDTO> getDepartmentStats(Long surveyId, Long departmentId) {
        Set<String> submittedEmployeeIds = (surveyId == null)
                ? responseRepository.findAll().stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet())
                : responseRepository.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());

        List<Department> departments = (departmentId != null)
                ? List.of(departmentRepository.findById(departmentId).orElseThrow(() -> new RuntimeException("Department not found")))
                : departmentRepository.findAll();

        return departments.stream()
                .map(d -> mapDepartmentStats(d, submittedEmployeeIds))
                .collect(Collectors.toList());
    }

    @Override
    public SurveyStatsDTO getSurveyStats(Long surveyId) {
        return buildSurveyStats(surveyId);
    }

    @Override
    public List<String> listSubmittedEmployees(Long surveyId, Long departmentId) {
        Set<String> submittedIds = (surveyId == null)
                ? responseRepository.findAll().stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet())
                : responseRepository.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());

        List<Employee> employees = (departmentId == null)
                ? employeeRepository.findAll()
                : employeeRepository.findByDepartmentId(departmentId);

        return employees.stream()
                .filter(e -> submittedIds.contains(e.getEmployeeId()))
                .map(Employee::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listPendingEmployees(Long surveyId, Long departmentId) {
        Set<String> submittedIds = (surveyId == null)
                ? Collections.emptySet()
                : responseRepository.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());

        List<Employee> employees = (departmentId == null)
                ? employeeRepository.findAll()
                : employeeRepository.findByDepartmentId(departmentId);

        // If surveyId == null we treat "pending" relative to ALL submissions:
        Set<String> allSubmitted = responseRepository.findAll().stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());

        Set<String> excludeSet = (surveyId == null) ? allSubmitted : submittedIds;

        return employees.stream()
                .filter(e -> !excludeSet.contains(e.getEmployeeId()))
                .map(Employee::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<Survey> recentSurveys(int limit) {
        // Use repository method for top N by publishedAt if available
        List<Survey> top = surveyRepository.findTop5ByOrderByPublishedAtDesc();
        if (limit <= 0 || limit >= top.size()) return top;
        return top.subList(0, limit);
    }

    // ---------- helper methods ----------
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
        s.setTotalPending(Math.max(0, totalEmployees - submitted));

        return s;
    }

    private DepartmentStatsDTO mapDepartmentStats(Department department, Set<String> submittedEmployeeIds) {
        DepartmentStatsDTO dto = new DepartmentStatsDTO();

        List<Employee> employees = department.getEmployees() == null ? List.of() : department.getEmployees();

        dto.setDepartmentName(department.getName());
        dto.setTotalEmployees(employees.size());

        long submitted = employees.stream()
                .filter(e -> submittedEmployeeIds.contains(e.getEmployeeId()))
                .count();

        dto.setSubmitted(submitted);
        dto.setPending(dto.getTotalEmployees() - dto.getSubmitted());

        return dto;
    }
}
