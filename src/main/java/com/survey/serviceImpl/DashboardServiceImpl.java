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
    private final SurveyAssignmentRepository surveyAssignmentRepo; // NEW

    @Override
    public DashboardResponseDTO getDashboardData(Long surveyId, Long departmentId) {

        DashboardResponseDTO dto = new DashboardResponseDTO();

        // ------------------------------
        // 1) Load Survey (if provided)
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
        // 2) Check explicit assignments (survey_assignments)
        //    If explicit assignments exist we will honor them.
        // ------------------------------
        List<SurveyAssignment> explicitAssignments = (surveyId == null)
                ? Collections.emptyList()
                : surveyAssignmentRepo.findBySurveyId(surveyId);

        // If departmentId filter provided, keep only assignments for that department
        if (departmentId != null && explicitAssignments != null && !explicitAssignments.isEmpty()) {
            explicitAssignments = explicitAssignments.stream()
                    .filter(sa -> sa.getDepartment() != null && Objects.equals(sa.getDepartment().getId(), departmentId))
                    .collect(Collectors.toList());
            // if after filtering explicitAssignments becomes empty -> it means the survey not assigned to that department
            if (explicitAssignments.isEmpty()) {
                dto.setError("NOT_ASSIGNED");
                return dto;
            }
        }

        // ------------------------------
        // 3) Build allowed employee list (based on explicit assignments OR department mapping fallback)
        // ------------------------------
        List<Employee> allowedEmployees = new ArrayList<>();

        if (explicitAssignments != null && !explicitAssignments.isEmpty()) {
            // Use explicit assignment entries (unique employeeIds)
            Set<String> empIds = explicitAssignments.stream()
                    .map(SurveyAssignment::getEmployeeId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (!empIds.isEmpty()) {
                // fetch employees by employeeId list (assumes employeeRepo has method findByEmployeeIdIn)
                // Otherwise fetch individually as fallback
                List<Employee> byIds = employeeRepo.findByEmployeeIdIn(new ArrayList<>(empIds));
                if (byIds == null || byIds.isEmpty()) {
                    // fallback: try per-id retrieval
                    for (String id : empIds) {
                        employeeRepo.findByEmployeeId(id).ifPresent(allowedEmployees::add);
                    }
                } else {
                    allowedEmployees.addAll(byIds);
                }
            }
        } else {
            // FALLBACK: department mapping (existing logic) + optional departmentId filter
            List<SurveyDepartmentMap> mappings =
                    (surveyId == null) ? new ArrayList<>() : surveyDepartmentMapRepo.findBySurveyId(surveyId);

            long totalDeptCount = deptRepo.count();
            boolean isAllDeptSurvey = (mappings.isEmpty() || mappings.size() == totalDeptCount);

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
                    return dto;
                }

                finalDepartments = List.of(dept);
            } else {
                if (isAllDeptSurvey) {
                    finalDepartments = deptRepo.findAll();
                } else {
                    finalDepartments = mappings.stream()
                            .map(SurveyDepartmentMap::getDepartment)
                            .collect(Collectors.toList());
                }
            }

            // now fetch employees for those departments
            for (Department d : finalDepartments) {
                allowedEmployees.addAll(employeeRepo.findByDepartmentId(d.getId()));
            }

            // If survey entity contains a targetPosition field (optional),
            // you might want to include employees by position as well.
            // (Not added here because Survey structure unknown — can be added easily.)
        }

        // ------------------------------
        // 4) De-duplicate & prepare sets
        // ------------------------------
        // dedupe by employeeId
        Map<String, Employee> empMap = new LinkedHashMap<>();
        for (Employee e : allowedEmployees) {
            if (e != null && e.getEmployeeId() != null) {
                empMap.putIfAbsent(e.getEmployeeId(), e);
            }
        }

        List<Employee> employees = new ArrayList<>(empMap.values());
        int totalEmployees = employees.size();

        Set<String> allowedEmpIds = employees.stream()
                .map(Employee::getEmployeeId)
                .collect(Collectors.toSet());

        // ------------------------------
        // 5) Responses (only consider allowed employees)
        // ------------------------------
        List<SurveyResponse> responses =
                (surveyId == null) ? responseRepo.findAll() : responseRepo.findBySurveyId(surveyId);

        Set<String> submitted = responses.stream()
                .map(SurveyResponse::getEmployeeId)
                .filter(allowedEmpIds::contains)
                .collect(Collectors.toSet());

        // ------------------------------
        // 6) Build submitted and pending lists (only allowed employees)
        // ------------------------------
        List<String> submittedList = employees.stream()
                .filter(e -> submitted.contains(e.getEmployeeId()))
                .map(e -> e.getName() + " - " + e.getEmployeeId())
                .collect(Collectors.toList());

        List<String> pendingList = employees.stream()
                .filter(e -> !submitted.contains(e.getEmployeeId()))
                .map(e -> e.getName() + " - " + e.getEmployeeId())
                .collect(Collectors.toList());

        // ------------------------------
        // 7) Department Stats — ensure stats reflect allowed employees
        // ------------------------------
        List<DepartmentStatsDTO> deptStats = new ArrayList<>();
        // If explicit assignments exist, compute per-department stats from assignments subset
        if (explicitAssignments != null && !explicitAssignments.isEmpty()) {
            // group assignments by dept
            Map<Long, List<SurveyAssignment>> grouped = explicitAssignments.stream()
                    .filter(sa -> sa.getDepartment() != null)
                    .collect(Collectors.groupingBy(sa -> sa.getDepartment().getId()));

            for (Map.Entry<Long, List<SurveyAssignment>> entry : grouped.entrySet()) {
                Department dept = deptRepo.findById(entry.getKey()).orElse(null);
                List<SurveyAssignment> assignList = entry.getValue();

                int total = assignList.size();
                long submittedCount = assignList.stream()
                        .map(SurveyAssignment::getEmployeeId)
                        .filter(submitted::contains)
                        .count();

                DepartmentStatsDTO ds = new DepartmentStatsDTO();
                ds.setDepartmentId(dept != null ? dept.getId() : null);
                ds.setDepartmentName(dept != null ? dept.getName() : ("Dept-" + entry.getKey()));
                ds.setTotalEmployees(total);
                ds.setSubmitted((int) submittedCount);
                ds.setPending(total - (int) submittedCount);
                ds.setResponseRate(total == 0 ? 0 : (100.0 * submittedCount / total));
                deptStats.add(ds);
            }

            // also include any assignments without department (optional)
            long unassignedCount = explicitAssignments.stream().filter(sa -> sa.getDepartment() == null).count();
            if (unassignedCount > 0) {
                DepartmentStatsDTO ds = new DepartmentStatsDTO();
                ds.setDepartmentId(null);
                ds.setDepartmentName("Unassigned/Individual");
                long submittedCount = explicitAssignments.stream()
                        .filter(sa -> sa.getDepartment() == null)
                        .map(SurveyAssignment::getEmployeeId)
                        .filter(submitted::contains).count();
                ds.setTotalEmployees((int) unassignedCount);
                ds.setSubmitted((int) submittedCount);
                ds.setPending((int) (unassignedCount - submittedCount));
                ds.setResponseRate(unassignedCount == 0 ? 0 : (100.0 * submittedCount / unassignedCount));
                deptStats.add(ds);
            }

        } else {
            // No explicit assignments — use finalDepartments / department mapping (existing behaviour)
            // Reconstruct finalDepartments used earlier:
            List<SurveyDepartmentMap> mappings = (surveyId == null) ? new ArrayList<>() : surveyDepartmentMapRepo.findBySurveyId(surveyId);
            long deptCount = deptRepo.count();
            boolean isAllDeptSurvey = (mappings.isEmpty() || mappings.size() == deptCount);
            List<Department> finalDepartments;
            if (mappings.isEmpty() && isAllDeptSurvey) {
                finalDepartments = deptRepo.findAll();
            } else if (mappings.isEmpty()) {
                finalDepartments = deptRepo.findAll();
            } else {
                finalDepartments = mappings.stream().map(SurveyDepartmentMap::getDepartment).collect(Collectors.toList());
            }

            for (Department d : finalDepartments) {
                DepartmentStatsDTO ds = createDeptStatsFilteredByAllowedEmployees(surveyId, d, allowedEmpIds);
                deptStats.add(ds);
            }
        }

        // ------------------------------
        // 8) Fill DTO
        // ------------------------------
        dto.setError(null);
        dto.setTotalEmployees(totalEmployees);
        dto.setTotalSubmitted(submittedList.size());
        dto.setTotalPending(pendingList.size());
        dto.setTotalSurveys(surveyRepo.count());
        dto.setDepartmentStats(deptStats);
        dto.setSurveyStats(getSurveyStatsFiltered(surveyId, allowedEmpIds));
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
     * Department stats builder using only allowed employees
     * -------------------------------------------------------- */
    private DepartmentStatsDTO createDeptStatsFilteredByAllowedEmployees(Long surveyId, Department dept, Set<String> allowedEmpIds) {

        // Employees for this department (but only those in allowedEmpIds)
        List<Employee> employees = employeeRepo.findByDepartmentId(dept.getId())
                .stream()
                .filter(e -> allowedEmpIds.contains(e.getEmployeeId()))
                .collect(Collectors.toList());

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
     * Survey Stats (filtered) — counts only allowed employees
     * -------------------------------------------------------- */
    private SurveyStatsDTO getSurveyStatsFiltered(Long surveyId, Set<String> allowedEmpIds) {
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

        // total is number of allowed employees (if filtered set provided) otherwise compute
        int total = (allowedEmpIds != null && !allowedEmpIds.isEmpty())
                ? allowedEmpIds.size()
                : deptRepo.findAll().stream()
                    .mapToInt(d -> employeeRepo.findByDepartmentId(d.getId()).size()).sum();

        long submitted = responseRepo.findBySurveyId(surveyId).stream()
                .map(SurveyResponse::getEmployeeId)
                .filter(id -> allowedEmpIds == null || allowedEmpIds.isEmpty() || allowedEmpIds.contains(id))
                .count();

        dto.setTotalEmployees(total);
        dto.setTotalSubmitted(submitted);
        dto.setTotalPending(total - (int) submitted);

        return dto;
    }

    @Override
    public List<DepartmentStatsDTO> getDepartmentStats(Long surveyId, Long departmentId) {
        return getDashboardData(surveyId, departmentId).getDepartmentStats();
    }

    @Override
    public SurveyStatsDTO getSurveyStats(Long surveyId) {
        // keep original signature but delegate to filtered variant
        return getSurveyStatsFiltered(surveyId, Collections.emptySet());
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
