package com.survey.serviceImpl;

import com.survey.dto.dashboard.*;
import com.survey.entity.*;
import com.survey.repository.*;
import com.survey.service.DashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final SurveyRepository surveyRepo;
    private final EmployeeRepository employeeRepo;
    private final SurveyResponseRepository responseRepo;
    private final DepartmentRepository deptRepo;
    private final SurveyDepartmentMapRepository surveyDepartmentMapRepo;

    @Override
    public DashboardResponseDTO getDashboardData(Long surveyId, Long departmentId) {

        DashboardResponseDTO dto = new DashboardResponseDTO();

        // ------------------------------
        // 1) Load Survey
        // ------------------------------
        Survey survey = null;
        if (surveyId != null) {
            survey = surveyRepo.findById(surveyId).orElse(null);
            if (survey == null) {
                dto.setError("SURVEY_NOT_FOUND");
                return dto;
            }
        }

        // ------------------------------
        // 2) Survey → Department mapping
        // ------------------------------
        List<SurveyDepartmentMap> mappings =
                (surveyId == null)
                        ? new ArrayList<>()
                        : surveyDepartmentMapRepo.findBySurveyId(surveyId);

        long totalDeptCount = deptRepo.count();
        boolean isAllDeptSurvey = (mappings.isEmpty() || mappings.size() == totalDeptCount);

        // ------------------------------
        // 3) Determine Final Departments
        // ------------------------------
        List<Department> finalDepartments;

        if (departmentId != null) {

            Department dept = deptRepo.findById(departmentId).orElse(null);
            if (dept == null) {
                dto.setError("DEPARTMENT_NOT_FOUND");
                return dto;
            }

            boolean allowed = isAllDeptSurvey ||
                    mappings.stream().anyMatch(m -> m.getDepartment().getId().equals(departmentId));

            if (!allowed) {
                dto.setError("NOT_ASSIGNED");
                return dto;   // ⭐ return clean response, no 500
            }

            finalDepartments = List.of(dept);

        } else {

            if (isAllDeptSurvey) {
                finalDepartments = deptRepo.findAll();
            } else {
                finalDepartments = mappings
                        .stream()
                        .map(SurveyDepartmentMap::getDepartment)
                        .toList();
            }
        }

        // ------------------------------
        // 4) Employees
        // ------------------------------
        List<Employee> employees = new ArrayList<>();
        finalDepartments.forEach(d ->
                employees.addAll(employeeRepo.findByDepartmentId(d.getId()))
        );

        int totalEmployees = employees.size();

        Set<String> allowedEmpIds =
                employees.stream().map(Employee::getEmployeeId).collect(Collectors.toSet());

        // ------------------------------
        // 5) Responses
        // ------------------------------
        List<SurveyResponse> responses =
                (surveyId == null)
                        ? responseRepo.findAll()
                        : responseRepo.findBySurveyId(surveyId);

        Set<String> submitted = responses.stream()
                .map(SurveyResponse::getEmployeeId)
                .filter(allowedEmpIds::contains)
                .collect(Collectors.toSet());

        // ------------------------------
        // 6) Build Lists
        // ------------------------------
        List<String> submittedList = employees.stream()
                .filter(e -> submitted.contains(e.getEmployeeId()))
                .map(e -> e.getName() + " - " + e.getEmployeeId())
                .toList();

        List<String> pendingList = employees.stream()
                .filter(e -> !submitted.contains(e.getEmployeeId()))
                .map(e -> e.getName() + " - " + e.getEmployeeId())
                .toList();

        // ------------------------------
        // 7) Department Stats
        // ------------------------------
        List<DepartmentStatsDTO> deptStats = finalDepartments.stream()
                .map(d -> createDeptStats(surveyId, d))
                .toList();

        // ------------------------------
        // 8) Fill DTO
        // ------------------------------
        dto.setError(null);
        dto.setTotalEmployees(totalEmployees);
        dto.setTotalSubmitted(submittedList.size());
        dto.setTotalPending(pendingList.size());
        dto.setTotalSurveys(surveyRepo.count());
        dto.setDepartmentStats(deptStats);
        dto.setSurveyStats(getSurveyStats(surveyId));
        dto.setSubmittedEmployees(submittedList);
        dto.setPendingEmployees(pendingList);

        return dto;
    }

    /* ----------------------------------------------------------
     * Build error response cleanly
     * -------------------------------------------------------- */
    private DashboardResponseDTO buildError(String message) {
        DashboardResponseDTO dto = new DashboardResponseDTO();
        dto.setError(message);
        return dto;
    }

    /* ----------------------------------------------------------
     * Department stats builder
     * -------------------------------------------------------- */
    private DepartmentStatsDTO createDeptStats(Long surveyId, Department dept) {

        List<Employee> employees = employeeRepo.findByDepartmentId(dept.getId());
        int total = employees.size();

        List<String> submittedIds = (surveyId == null)
                ? responseRepo.findAll().stream().map(SurveyResponse::getEmployeeId).toList()
                : responseRepo.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).toList();

        long submittedCount = employees.stream()
                .filter(e -> submittedIds.contains(e.getEmployeeId()))
                .count();

        DepartmentStatsDTO dto = new DepartmentStatsDTO();
        dto.setDepartmentId(dept.getId());
        dto.setDepartmentName(dept.getName());
        dto.setTotalEmployees(total);
        dto.setSubmitted((int) submittedCount);
        dto.setPending(total - (int) submittedCount);
        dto.setResponseRate(total == 0 ? 0 : (100.0 * submittedCount / total));

        return dto;
    }

    /* ----------------------------------------------------------
     * Other methods
     * -------------------------------------------------------- */
    @Override
    public List<DepartmentStatsDTO> getDepartmentStats(Long surveyId, Long departmentId) {
        return getDashboardData(surveyId, departmentId).getDepartmentStats();
    }

    @Override
    public SurveyStatsDTO getSurveyStats(Long surveyId) {

        SurveyStatsDTO dto = new SurveyStatsDTO();

        if (surveyId == null) {
            int total = employeeRepo.findAll().size();
            long submitted = responseRepo.count();

            dto.setSurveyId(0L);
            dto.setSurveyTitle("All Surveys");
            dto.setTotalEmployees(total);
            dto.setTotalSubmitted(submitted);
            dto.setTotalPending(total - submitted);

            return dto;
        }

        Survey survey = surveyRepo.findById(surveyId).orElse(null);
        dto.setSurveyId(surveyId);
        dto.setSurveyTitle(survey != null ? survey.getTitle() : "Survey");

        List<SurveyDepartmentMap> maps = surveyDepartmentMapRepo.findBySurveyId(surveyId);
        long deptCount = deptRepo.count();

        List<Department> depts =
                (maps.isEmpty() || maps.size() == deptCount)
                        ? deptRepo.findAll()
                        : maps.stream().map(SurveyDepartmentMap::getDepartment).toList();

        int total = depts.stream()
                .mapToInt(d -> employeeRepo.findByDepartmentId(d.getId()).size())
                .sum();

        long submitted = responseRepo.countBySurveyId(surveyId);

        dto.setTotalEmployees(total);
        dto.setTotalSubmitted(submitted);
        dto.setTotalPending(total - submitted);

        return dto;
    }

    @Override
    public List<String> listSubmittedEmployees(Long surveyId, Long departmentId) {
        return getDashboardData(surveyId, departmentId).getSubmittedEmployees();
    }

    @Override
    public List<String> listPendingEmployees(Long surveyId, Long departmentId) {
        return getDashboardData(surveyId, departmentId).getPendingEmployees();
    }

    @Override
    public List<Survey> listSurveys() {
        return surveyRepo.findAll();
    }

    @Override
    public List<Department> listDepartments() {
        return deptRepo.findAll();
    }

    @Override
    public List<Survey> recentSurveys(int limit) {
        return surveyRepo.findAll()
                .stream()
                .sorted(Comparator.comparing(Survey::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
