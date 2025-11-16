package com.survey.serviceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.survey.dto.dashboard.DashboardResponseDTO;
import com.survey.dto.dashboard.DepartmentStatsDTO;
import com.survey.dto.dashboard.SurveyStatsDTO;
import com.survey.entity.Department;
import com.survey.entity.Employee;
import com.survey.entity.Survey;
import com.survey.entity.SurveyResponse;
import com.survey.repository.DepartmentRepository;
import com.survey.repository.EmployeeRepository;
import com.survey.repository.SurveyRepository;
import com.survey.repository.SurveyResponseRepository;
import com.survey.service.DashboardService;

import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class DashboardServiceImpl implements DashboardService {
//
//    private final EmployeeRepository employeeRepository;
//    private final SurveyRepository surveyRepository;
//    private final SurveyResponseRepository responseRepository;
//    private final DepartmentRepository departmentRepository;
//
//    @Override
//    public DashboardResponseDTO getDashboardData(Long surveyId, Long departmentId) {
//
//        DashboardResponseDTO dto = new DashboardResponseDTO();
//
//        long totalEmployees = employeeRepository.count();
//        dto.setTotalEmployees(totalEmployees);
//        dto.setTotalSurveys(surveyRepository.count());
//
//        // total submitted depends on survey filter
//        long totalSubmitted = (surveyId == null)
//                ? responseRepository.count()
//                : responseRepository.countBySurveyId(surveyId);
//
//        dto.setTotalSubmitted(totalSubmitted);
//        dto.setTotalPending(Math.max(0, totalEmployees - totalSubmitted));
//
//        // build submitted ids set (survey or all)
//        Set<String> submittedEmployeeIds = (surveyId == null)
//                ? responseRepository.findAll().stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet())
//                : responseRepository.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());
//
//        // departments list (filtered or all)
//        List<Department> departments = (departmentId != null)
//                ? List.of(departmentRepository.findById(departmentId).orElseThrow(() -> new RuntimeException("Department not found")))
//                : departmentRepository.findAll();
//
//        List<DepartmentStatsDTO> deptStats = departments.stream()
//                .map(d -> mapDepartmentStats(d, submittedEmployeeIds))
//                .collect(Collectors.toList());
//        dto.setDepartmentStats(deptStats);
//
//        // survey stats (if survey filter)
//        if (surveyId != null) {
//            dto.setSurveyStats(buildSurveyStats(surveyId));
//        }
//
//        return dto;
//    }
//
//    @Override
//    public List<Survey> listSurveys() {
//    	return surveyRepository.findAll()
//    	        .stream()
//    	        .sorted(Comparator.comparing(Survey::getCreatedAt).reversed())
//    	        .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<Department> listDepartments() {
//        return departmentRepository.findAll();
//    }
//
//    @Override
//    public List<DepartmentStatsDTO> getDepartmentStats(Long surveyId, Long departmentId) {
//        Set<String> submittedEmployeeIds = (surveyId == null)
//                ? responseRepository.findAll().stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet())
//                : responseRepository.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());
//
//        List<Department> departments = (departmentId != null)
//                ? List.of(departmentRepository.findById(departmentId).orElseThrow(() -> new RuntimeException("Department not found")))
//                : departmentRepository.findAll();
//
//        return departments.stream()
//                .map(d -> mapDepartmentStats(d, submittedEmployeeIds))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public SurveyStatsDTO getSurveyStats(Long surveyId) {
//        return buildSurveyStats(surveyId);
//    }
//
//    @Override
//    public List<String> listSubmittedEmployees(Long surveyId, Long departmentId) {
//        Set<String> submittedIds = (surveyId == null)
//                ? responseRepository.findAll().stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet())
//                : responseRepository.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());
//
//        List<Employee> employees = (departmentId == null)
//                ? employeeRepository.findAll()
//                : employeeRepository.findByDepartmentId(departmentId);
//
//        return employees.stream()
//                .filter(e -> submittedIds.contains(e.getEmployeeId()))
//                .map(Employee::getName)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<String> listPendingEmployees(Long surveyId, Long departmentId) {
//        Set<String> submittedIds = (surveyId == null)
//                ? Collections.emptySet()
//                : responseRepository.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());
//
//        List<Employee> employees = (departmentId == null)
//                ? employeeRepository.findAll()
//                : employeeRepository.findByDepartmentId(departmentId);
//
//        // If surveyId == null we treat "pending" relative to ALL submissions:
//        Set<String> allSubmitted = responseRepository.findAll().stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());
//
//        Set<String> excludeSet = (surveyId == null) ? allSubmitted : submittedIds;
//
//        return employees.stream()
//                .filter(e -> !excludeSet.contains(e.getEmployeeId()))
//                .map(Employee::getName)
//                .collect(Collectors.toList());
//    }
//
////    @Override
////    public List<Survey> recentSurveys(int limit) {
////        // Use repository method for top N by publishedAt if available
////        List<Survey> top = surveyRepository.findTop5ByOrderByPublishedAtDesc();
////        if (limit <= 0 || limit >= top.size()) return top;
////        return top.subList(0, limit);
////    }
//    
//    @Override
//    public List<Survey> recentSurveys(int limit) {
//
//        List<Survey> all = surveyRepository.findAll();
//
//        // Sort null publishedAt last to avoid NPE
//        List<Survey> sorted = all.stream()
//                .sorted((a, b) -> {
//                    if (a.getPublishedAt() == null && b.getPublishedAt() == null) return 0;
//                    if (a.getPublishedAt() == null) return 1; // null = bottom
//                    if (b.getPublishedAt() == null) return -1;
//                    return b.getPublishedAt().compareTo(a.getPublishedAt()); // DESC
//                })
//                .limit(limit)
//                .collect(Collectors.toList());
//
//        return sorted;
//    }
//
//
//
//    // ---------- helper methods ----------
//    private SurveyStatsDTO buildSurveyStats(Long surveyId) {
//        Survey survey = surveyRepository.findById(surveyId)
//                .orElseThrow(() -> new RuntimeException("Survey not found"));
//
//        SurveyStatsDTO s = new SurveyStatsDTO();
//        s.setSurveyId(survey.getId());
//        s.setSurveyTitle(survey.getTitle());
//
//        long totalEmployees = employeeRepository.count();
//        long submitted = responseRepository.countBySurveyId(surveyId);
//
//        s.setTotalEmployees(totalEmployees);
//        s.setTotalSubmitted(submitted);
//        s.setTotalPending(Math.max(0, totalEmployees - submitted));
//
//        return s;
//    }
//
//    private DepartmentStatsDTO mapDepartmentStats(Department department, Set<String> submittedEmployeeIds) {
//        DepartmentStatsDTO dto = new DepartmentStatsDTO();
//
////        List<Employee> employees = department.getEmployees() == null ? List.of() : department.getEmployees();
//        List<Employee> employees = employeeRepository.findByDepartmentId(department.getId());
//
//
//        dto.setDepartmentName(department.getName());
//        dto.setTotalEmployees(employees.size());
//
//        long submitted = employees.stream()
//                .filter(e -> submittedEmployeeIds.contains(e.getEmployeeId()))
//                .count();
//
//        dto.setSubmitted(submitted);
//        dto.setPending(dto.getTotalEmployees() - dto.getSubmitted());
//
//        return dto;
//    }
//}


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

        long submittedCount = (surveyId == null)
                ? responseRepository.count()
                : responseRepository.countBySurveyId(surveyId);

        dto.setTotalSubmitted(submittedCount);
        dto.setTotalPending(Math.max(0, totalEmployees - submittedCount));

        // Build submitted ID set
        Set<String> submittedIds = (surveyId == null)
                ? responseRepository.findAll().stream()
                        .map(SurveyResponse::getEmployeeId)
                        .collect(Collectors.toSet())
                : responseRepository.findBySurveyId(surveyId).stream()
                        .map(SurveyResponse::getEmployeeId)
                        .collect(Collectors.toSet());

        // Departments (all or filtered)
        List<Department> departments = (departmentId != null)
                ? List.of(departmentRepository.findById(departmentId)
                        .orElseThrow(() -> new RuntimeException("Department not found")))
                : departmentRepository.findAll();

        List<DepartmentStatsDTO> stats = departments.stream()
                .map(d -> mapDepartmentStats(d, submittedIds))
                .collect(Collectors.toList());

        dto.setDepartmentStats(stats);

        // Add survey stats only if surveyId filter is provided
        if (surveyId != null) {
            dto.setSurveyStats(this.getSurveyStats(surveyId));
        }

        return dto;
    }


    @Override
    public List<DepartmentStatsDTO> getDepartmentStats(Long surveyId, Long departmentId) {

        // Submitted IDs set
        Set<String> submittedIds = (surveyId == null)
                ? responseRepository.findAll().stream()
                        .map(SurveyResponse::getEmployeeId)
                        .collect(Collectors.toSet())
                : responseRepository.findBySurveyId(surveyId).stream()
                        .map(SurveyResponse::getEmployeeId)
                        .collect(Collectors.toSet());

        // Departments
        List<Department> departments = (departmentId != null)
                ? List.of(departmentRepository.findById(departmentId)
                        .orElseThrow(() -> new RuntimeException("Department not found")))
                : departmentRepository.findAll();

        return departments.stream()
                .map(d -> mapDepartmentStats(d, submittedIds))
                .collect(Collectors.toList());
    }

    @Override
    public SurveyStatsDTO getSurveyStats(Long surveyId) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        SurveyStatsDTO dto = new SurveyStatsDTO();
        dto.setSurveyId(survey.getId());
        dto.setSurveyTitle(survey.getTitle());

        long totalEmployees = employeeRepository.count();
        long submitted = responseRepository.countBySurveyId(surveyId);

        dto.setTotalEmployees(totalEmployees);
        dto.setTotalSubmitted(submitted);
        dto.setTotalPending(totalEmployees - submitted);

        return dto;
    }


    @Override
    public List<String> listSubmittedEmployees(Long surveyId, Long departmentId) {

        Set<String> submittedIds = (surveyId == null)
                ? responseRepository.findAll().stream()
                        .map(SurveyResponse::getEmployeeId)
                        .collect(Collectors.toSet())
                : responseRepository.findBySurveyId(surveyId).stream()
                        .map(SurveyResponse::getEmployeeId)
                        .collect(Collectors.toSet());

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
                : responseRepository.findBySurveyId(surveyId)
                        .stream().map(SurveyResponse::getEmployeeId)
                        .collect(Collectors.toSet());

        List<Employee> employees = (departmentId == null)
                ? employeeRepository.findAll()
                : employeeRepository.findByDepartmentId(departmentId);

        return employees.stream()
                .filter(e -> !submittedIds.contains(e.getEmployeeId()))
                .map(Employee::getName)
                .collect(Collectors.toList());
    }


    // ========== Helpers ==========

    private DepartmentStatsDTO mapDepartmentStats(Department department, Set<String> submittedIds) {

        DepartmentStatsDTO dto = new DepartmentStatsDTO();
        dto.setDepartmentName(department.getName());

        List<Employee> employees = department.getEmployees() != null
                ? department.getEmployees()
                : List.of();

        dto.setTotalEmployees(employees.size());

        long submitted = employees.stream()
                .filter(e -> submittedIds.contains(e.getEmployeeId()))
                .count();

        dto.setSubmitted(submitted);
        dto.setPending(employees.size() - submitted);

        return dto;
    }
}
