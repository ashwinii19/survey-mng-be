package com.survey.serviceImpl;
////
////import java.util.Collections;
////import java.util.List;
////import java.util.Set;
////import java.util.stream.Collectors;
////
////import org.springframework.stereotype.Service;
////
////import com.survey.dto.dashboard.DashboardResponseDTO;
////import com.survey.dto.dashboard.DepartmentStatsDTO;
////import com.survey.dto.dashboard.SurveyStatsDTO;
////import com.survey.entity.Department;
////import com.survey.entity.Employee;
////import com.survey.entity.Survey;
////import com.survey.entity.SurveyResponse;
////import com.survey.repository.DepartmentRepository;
////import com.survey.repository.EmployeeRepository;
////import com.survey.repository.SurveyRepository;
////import com.survey.repository.SurveyResponseRepository;
////import com.survey.service.DashboardService;
////
////import lombok.RequiredArgsConstructor;
//////
//////@Service
//////@RequiredArgsConstructor
//////public class DashboardServiceImpl implements DashboardService {
//////
//////    private final EmployeeRepository employeeRepository;
//////    private final SurveyRepository surveyRepository;
//////    private final SurveyResponseRepository responseRepository;
//////    private final DepartmentRepository departmentRepository;
//////
//////    @Override
//////    public DashboardResponseDTO getDashboardData(Long surveyId, Long departmentId) {
//////
//////        DashboardResponseDTO dto = new DashboardResponseDTO();
//////
//////        long totalEmployees = employeeRepository.count();
//////        dto.setTotalEmployees(totalEmployees);
//////        dto.setTotalSurveys(surveyRepository.count());
//////
//////        // total submitted depends on survey filter
//////        long totalSubmitted = (surveyId == null)
//////                ? responseRepository.count()
//////                : responseRepository.countBySurveyId(surveyId);
//////
//////        dto.setTotalSubmitted(totalSubmitted);
//////        dto.setTotalPending(Math.max(0, totalEmployees - totalSubmitted));
//////
//////        // build submitted ids set (survey or all)
//////        Set<String> submittedEmployeeIds = (surveyId == null)
//////                ? responseRepository.findAll().stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet())
//////                : responseRepository.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());
//////
//////        // departments list (filtered or all)
//////        List<Department> departments = (departmentId != null)
//////                ? List.of(departmentRepository.findById(departmentId).orElseThrow(() -> new RuntimeException("Department not found")))
//////                : departmentRepository.findAll();
//////
//////        List<DepartmentStatsDTO> deptStats = departments.stream()
//////                .map(d -> mapDepartmentStats(d, submittedEmployeeIds))
//////                .collect(Collectors.toList());
//////        dto.setDepartmentStats(deptStats);
//////
//////        // survey stats (if survey filter)
//////        if (surveyId != null) {
//////            dto.setSurveyStats(buildSurveyStats(surveyId));
//////        }
//////
//////        return dto;
//////    }
//////
//////    @Override
//////    public List<Survey> listSurveys() {
//////    	return surveyRepository.findAll()
//////    	        .stream()
//////    	        .sorted(Comparator.comparing(Survey::getCreatedAt).reversed())
//////    	        .collect(Collectors.toList());
//////    }
//////
//////    @Override
//////    public List<Department> listDepartments() {
//////        return departmentRepository.findAll();
//////    }
//////
//////    @Override
//////    public List<DepartmentStatsDTO> getDepartmentStats(Long surveyId, Long departmentId) {
//////        Set<String> submittedEmployeeIds = (surveyId == null)
//////                ? responseRepository.findAll().stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet())
//////                : responseRepository.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());
//////
//////        List<Department> departments = (departmentId != null)
//////                ? List.of(departmentRepository.findById(departmentId).orElseThrow(() -> new RuntimeException("Department not found")))
//////                : departmentRepository.findAll();
//////
//////        return departments.stream()
//////                .map(d -> mapDepartmentStats(d, submittedEmployeeIds))
//////                .collect(Collectors.toList());
//////    }
//////
//////    @Override
//////    public SurveyStatsDTO getSurveyStats(Long surveyId) {
//////        return buildSurveyStats(surveyId);
//////    }
//////
//////    @Override
//////    public List<String> listSubmittedEmployees(Long surveyId, Long departmentId) {
//////        Set<String> submittedIds = (surveyId == null)
//////                ? responseRepository.findAll().stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet())
//////                : responseRepository.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());
//////
//////        List<Employee> employees = (departmentId == null)
//////                ? employeeRepository.findAll()
//////                : employeeRepository.findByDepartmentId(departmentId);
//////
//////        return employees.stream()
//////                .filter(e -> submittedIds.contains(e.getEmployeeId()))
//////                .map(Employee::getName)
//////                .collect(Collectors.toList());
//////    }
//////
//////    @Override
//////    public List<String> listPendingEmployees(Long surveyId, Long departmentId) {
//////        Set<String> submittedIds = (surveyId == null)
//////                ? Collections.emptySet()
//////                : responseRepository.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());
//////
//////        List<Employee> employees = (departmentId == null)
//////                ? employeeRepository.findAll()
//////                : employeeRepository.findByDepartmentId(departmentId);
//////
//////        // If surveyId == null we treat "pending" relative to ALL submissions:
//////        Set<String> allSubmitted = responseRepository.findAll().stream().map(SurveyResponse::getEmployeeId).collect(Collectors.toSet());
//////
//////        Set<String> excludeSet = (surveyId == null) ? allSubmitted : submittedIds;
//////
//////        return employees.stream()
//////                .filter(e -> !excludeSet.contains(e.getEmployeeId()))
//////                .map(Employee::getName)
//////                .collect(Collectors.toList());
//////    }
//////
////////    @Override
////////    public List<Survey> recentSurveys(int limit) {
////////        // Use repository method for top N by publishedAt if available
////////        List<Survey> top = surveyRepository.findTop5ByOrderByPublishedAtDesc();
////////        if (limit <= 0 || limit >= top.size()) return top;
////////        return top.subList(0, limit);
////////    }
//////    
//////    @Override
//////    public List<Survey> recentSurveys(int limit) {
//////
//////        List<Survey> all = surveyRepository.findAll();
//////
//////        // Sort null publishedAt last to avoid NPE
//////        List<Survey> sorted = all.stream()
//////                .sorted((a, b) -> {
//////                    if (a.getPublishedAt() == null && b.getPublishedAt() == null) return 0;
//////                    if (a.getPublishedAt() == null) return 1; // null = bottom
//////                    if (b.getPublishedAt() == null) return -1;
//////                    return b.getPublishedAt().compareTo(a.getPublishedAt()); // DESC
//////                })
//////                .limit(limit)
//////                .collect(Collectors.toList());
//////
//////        return sorted;
//////    }
//////
//////
//////
//////    // ---------- helper methods ----------
//////    private SurveyStatsDTO buildSurveyStats(Long surveyId) {
//////        Survey survey = surveyRepository.findById(surveyId)
//////                .orElseThrow(() -> new RuntimeException("Survey not found"));
//////
//////        SurveyStatsDTO s = new SurveyStatsDTO();
//////        s.setSurveyId(survey.getId());
//////        s.setSurveyTitle(survey.getTitle());
//////
//////        long totalEmployees = employeeRepository.count();
//////        long submitted = responseRepository.countBySurveyId(surveyId);
//////
//////        s.setTotalEmployees(totalEmployees);
//////        s.setTotalSubmitted(submitted);
//////        s.setTotalPending(Math.max(0, totalEmployees - submitted));
//////
//////        return s;
//////    }
//////
//////    private DepartmentStatsDTO mapDepartmentStats(Department department, Set<String> submittedEmployeeIds) {
//////        DepartmentStatsDTO dto = new DepartmentStatsDTO();
//////
////////        List<Employee> employees = department.getEmployees() == null ? List.of() : department.getEmployees();
//////        List<Employee> employees = employeeRepository.findByDepartmentId(department.getId());
//////
//////
//////        dto.setDepartmentName(department.getName());
//////        dto.setTotalEmployees(employees.size());
//////
//////        long submitted = employees.stream()
//////                .filter(e -> submittedEmployeeIds.contains(e.getEmployeeId()))
//////                .count();
//////
//////        dto.setSubmitted(submitted);
//////        dto.setPending(dto.getTotalEmployees() - dto.getSubmitted());
//////
//////        return dto;
//////    }
//////}
////
////
////@Service
////@RequiredArgsConstructor
////public class DashboardServiceImpl implements DashboardService {
////
////    private final EmployeeRepository employeeRepository;
////    private final SurveyRepository surveyRepository;
////    private final SurveyResponseRepository responseRepository;
////    private final DepartmentRepository departmentRepository;
////
////    @Override
////    public DashboardResponseDTO getDashboardData(Long surveyId, Long departmentId) {
////
////        DashboardResponseDTO dto = new DashboardResponseDTO();
////
////        long totalEmployees = employeeRepository.count();
////        dto.setTotalEmployees(totalEmployees);
////
////        dto.setTotalSurveys(surveyRepository.count());
////
////        long submittedCount = (surveyId == null)
////                ? responseRepository.count()
////                : responseRepository.countBySurveyId(surveyId);
////
////        dto.setTotalSubmitted(submittedCount);
////        dto.setTotalPending(Math.max(0, totalEmployees - submittedCount));
////
////        // Build submitted ID set
////        Set<String> submittedIds = (surveyId == null)
////                ? responseRepository.findAll().stream()
////                        .map(SurveyResponse::getEmployeeId)
////                        .collect(Collectors.toSet())
////                : responseRepository.findBySurveyId(surveyId).stream()
////                        .map(SurveyResponse::getEmployeeId)
////                        .collect(Collectors.toSet());
////
////        // Departments (all or filtered)
////        List<Department> departments = (departmentId != null)
////                ? List.of(departmentRepository.findById(departmentId)
////                        .orElseThrow(() -> new RuntimeException("Department not found")))
////                : departmentRepository.findAll();
////
////        List<DepartmentStatsDTO> stats = departments.stream()
////                .map(d -> mapDepartmentStats(d, submittedIds))
////                .collect(Collectors.toList());
////
////        dto.setDepartmentStats(stats);
////
////        // Add survey stats only if surveyId filter is provided
////        if (surveyId != null) {
////            dto.setSurveyStats(this.getSurveyStats(surveyId));
////        }
////
////        return dto;
////    }
////
////
////    @Override
////    public List<DepartmentStatsDTO> getDepartmentStats(Long surveyId, Long departmentId) {
////
////        // Submitted IDs set
////        Set<String> submittedIds = (surveyId == null)
////                ? responseRepository.findAll().stream()
////                        .map(SurveyResponse::getEmployeeId)
////                        .collect(Collectors.toSet())
////                : responseRepository.findBySurveyId(surveyId).stream()
////                        .map(SurveyResponse::getEmployeeId)
////                        .collect(Collectors.toSet());
////
////        // Departments
////        List<Department> departments = (departmentId != null)
////                ? List.of(departmentRepository.findById(departmentId)
////                        .orElseThrow(() -> new RuntimeException("Department not found")))
////                : departmentRepository.findAll();
////
////        return departments.stream()
////                .map(d -> mapDepartmentStats(d, submittedIds))
////                .collect(Collectors.toList());
////    }
////
////    @Override
////    public SurveyStatsDTO getSurveyStats(Long surveyId) {
////
////        Survey survey = surveyRepository.findById(surveyId)
////                .orElseThrow(() -> new RuntimeException("Survey not found"));
////
////        SurveyStatsDTO dto = new SurveyStatsDTO();
////        dto.setSurveyId(survey.getId());
////        dto.setSurveyTitle(survey.getTitle());
////
////        long totalEmployees = employeeRepository.count();
////        long submitted = responseRepository.countBySurveyId(surveyId);
////
////        dto.setTotalEmployees(totalEmployees);
////        dto.setTotalSubmitted(submitted);
////        dto.setTotalPending(totalEmployees - submitted);
////
////        return dto;
////    }
////
////
////    @Override
////    public List<String> listSubmittedEmployees(Long surveyId, Long departmentId) {
////
////        Set<String> submittedIds = (surveyId == null)
////                ? responseRepository.findAll().stream()
////                        .map(SurveyResponse::getEmployeeId)
////                        .collect(Collectors.toSet())
////                : responseRepository.findBySurveyId(surveyId).stream()
////                        .map(SurveyResponse::getEmployeeId)
////                        .collect(Collectors.toSet());
////
////        List<Employee> employees = (departmentId == null)
////                ? employeeRepository.findAll()
////                : employeeRepository.findByDepartmentId(departmentId);
////
////        return employees.stream()
////                .filter(e -> submittedIds.contains(e.getEmployeeId()))
////                .map(Employee::getName)
////                .collect(Collectors.toList());
////    }
////
////
////    @Override
////    public List<String> listPendingEmployees(Long surveyId, Long departmentId) {
////
////        Set<String> submittedIds = (surveyId == null)
////                ? Collections.emptySet()
////                : responseRepository.findBySurveyId(surveyId)
////                        .stream().map(SurveyResponse::getEmployeeId)
////                        .collect(Collectors.toSet());
////
////        List<Employee> employees = (departmentId == null)
////                ? employeeRepository.findAll()
////                : employeeRepository.findByDepartmentId(departmentId);
////
////        return employees.stream()
////                .filter(e -> !submittedIds.contains(e.getEmployeeId()))
////                .map(Employee::getName)
////                .collect(Collectors.toList());
////    }
////
////
////    // ========== Helpers ==========
////
////    private DepartmentStatsDTO mapDepartmentStats(Department department, Set<String> submittedIds) {
////
////        DepartmentStatsDTO dto = new DepartmentStatsDTO();
////        dto.setDepartmentName(department.getName());
////
////        List<Employee> employees = department.getEmployees() != null
////                ? department.getEmployees()
////                : List.of();
////
////        dto.setTotalEmployees(employees.size());
////
////        long submitted = employees.stream()
////                .filter(e -> submittedIds.contains(e.getEmployeeId()))
////                .count();
////
////        dto.setSubmitted(submitted);
////        dto.setPending(employees.size() - submitted);
////
////        return dto;
////    }
////}
//
//
//
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.springframework.stereotype.Service;
//
//import com.survey.dto.dashboard.DashboardResponseDTO;
//import com.survey.dto.dashboard.DepartmentStatsDTO;
//import com.survey.entity.Department;
//import com.survey.entity.Employee;
//import com.survey.entity.Survey;
//import com.survey.entity.SurveyResponse;
//import com.survey.repository.DepartmentRepository;
//import com.survey.repository.EmployeeRepository;
//import com.survey.repository.SurveyRepository;
//import com.survey.repository.SurveyResponseRepository;
//import com.survey.service.DashboardService;
//
//@Service
//public class DashboardServiceImpl implements DashboardService {
//
//    // TODO: Inject your repositories (SurveyRepository, DepartmentRepository, ResponseRepository, EmployeeRepository etc.)
//    // Example:
//    // private final SurveyRepository surveyRepo;
//    // private final ResponseRepository responseRepo;
//    // public DashboardServiceImpl(SurveyRepository surveyRepo, ResponseRepository responseRepo) { ... }
//
//    @Override
//    public DashboardResponseDTO getDashboardData(Long surveyId, Long departmentId) {
//        // Example/mock implementation — replace with DB queries
//        DashboardResponseDTO dto = new DashboardResponseDTO();
//
//        // department stats (mock)
//        List<DepartmentStatsDTO> deptStats = new ArrayList<>();
//        DepartmentStatsDTO d1 = new DepartmentStatsDTO();
//        d1.setDepartmentId(1L);
//        d1.setDepartmentName("HR");
//        d1.setTotalEmployees(50);
//        d1.setSubmitted(20);
//        d1.setPending(30);
//        d1.setResponseRate( (d1.getTotalEmployees() == 0) ? 0 : (100.0 * d1.getSubmitted() / d1.getTotalEmployees()) );
//
//        DepartmentStatsDTO d2 = new DepartmentStatsDTO();
//        d2.setDepartmentId(2L);
//        d2.setDepartmentName("IT");
//        d2.setTotalEmployees(80);
//        d2.setSubmitted(60);
//        d2.setPending(20);
//        d2.setResponseRate( (d2.getTotalEmployees() == 0) ? 0 : (100.0 * d2.getSubmitted() / d2.getTotalEmployees()) );
//
//        deptStats.add(d1); deptStats.add(d2);
//
//        // filter if departmentId provided
//        if (departmentId != null) {
//            deptStats = deptStats.stream()
//                .filter(ds -> ds.getDepartmentId().equals(departmentId))
//                .collect(Collectors.toList());
//        }
//
//        // survey stats (mock)
//        SurveyStatsDTO s = new SurveyStatsDTO();
//        s.setSurveyId(surveyId != null ? surveyId : 100L);
//        s.setSurveyTitle("Quarterly Survey");
//        s.setTotalEmployees(deptStats.stream().mapToInt(DepartmentStatsDTO::getTotalEmployees).sum());
//        s.setTotalSubmitted(deptStats.stream().mapToInt(DepartmentStatsDTO::getSubmitted).sum());
//        s.setTotalPending(deptStats.stream().mapToInt(DepartmentStatsDTO::getPending).sum());
//
//        // submitted / pending employee lists (mock strings)
//        List<String> submitted = new ArrayList<>();
//        submitted.add("John Doe - E101");
//        submitted.add("Jane Smith - E102");
//
//        List<String> pending = new ArrayList<>();
//        pending.add("Paul Admin - E201");
//        pending.add("Ria Gupta - E202");
//
//        // assemble dto
//        dto.setDepartmentStats(deptStats);
//        dto.setSurveyStats(s);
//        dto.setTotalEmployees(s.getTotalEmployees());
//        dto.setTotalSubmitted(s.getTotalSubmitted());
//        dto.setTotalPending(s.getTotalPending());
//        dto.setSubmittedEmployees(submitted);
//        dto.setPendingEmployees(pending);
//
//        return dto;
//    }
//
//    @Override
//    public List<DepartmentStatsDTO> getDepartmentStats(Long surveyId, Long departmentId) {
//        return getDashboardData(surveyId, departmentId).getDepartmentStats();
//    }
//
//    @Override
//    public SurveyStatsDTO getSurveyStats(Long id) {
//        return getDashboardData(id, null).getSurveyStats();
//    }
//
//    @Override
//    public List<String> listSubmittedEmployees(Long surveyId, Long departmentId) {
//        return getDashboardData(surveyId, departmentId).getSubmittedEmployees();
//    }
//
//    @Override
//    public List<String> listPendingEmployees(Long surveyId, Long departmentId) {
//        return getDashboardData(surveyId, departmentId).getPendingEmployees();
//    }
//
//    @Override
//    public java.util.List<Survey> listSurveys() {
//        // TODO: return actual surveys from DB
//        Survey s1 = new Survey(); s1.setId(100L); s1.setTitle("Quarterly Survey");
//        return Arrays.asList(s1);
//    }
//
//    @Override
//    public java.util.List<Department> listDepartments() {
//        // TODO: return actual departments from DB
//        Department d1 = new Department(); d1.setId(1L); d1.setName("HR");
//        Department d2 = new Department(); d2.setId(2L); d2.setName("IT");
//        return Arrays.asList(d1, d2);
//    }
//
//    @Override
//    public java.util.List<Survey> recentSurveys(int limit) {
//        return listSurveys().stream().limit(limit).collect(Collectors.toList());
//    }
//}
//



import com.survey.dto.dashboard.*;
import com.survey.entity.Department;
import com.survey.entity.Employee;
import com.survey.entity.Survey;
import com.survey.entity.SurveyResponse;
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

    @Override
    public DashboardResponseDTO getDashboardData(Long surveyId, Long departmentId) {

        // -----------------------------
        // 1️⃣ If surveyId is NULL -> Show ALL data (no filter)
        // -----------------------------
        Survey survey = null;

        if (surveyId != null) {
            survey = surveyRepo.findById(surveyId)
                    .orElse(null); // Do NOT throw — works smoothly
        }

        // -----------------------------
        // 2️⃣ Determine department to filter
        // -----------------------------
        Long targetDeptId = null;

        if (departmentId != null) {
            targetDeptId = departmentId;
        } else if (survey != null && survey.getTargetDepartment() != null) {
            targetDeptId = survey.getTargetDepartment().getId();
        }

        // -----------------------------
        // 3️⃣ Employees (filtered or ALL)
        // -----------------------------
        List<Employee> employees =
                (targetDeptId == null)
                        ? employeeRepo.findAll()
                        : employeeRepo.findByDepartmentId(targetDeptId);

        int totalEmployees = employees.size();

        // -----------------------------
        // 4️⃣ Submitted responses
        // -----------------------------
        List<SurveyResponse> responses =
                (surveyId == null)
                        ? responseRepo.findAll()   // No filter
                        : responseRepo.findBySurveyId(surveyId);

        Set<String> submittedEmployeeIds = responses.stream()
                .map(SurveyResponse::getEmployeeId)
                .collect(Collectors.toSet());

        int totalSubmitted = (int) submittedEmployeeIds.size();

        // -----------------------------
        // 5️⃣ Pending employees
        // -----------------------------
        List<String> pendingEmployees = employees.stream()
                .filter(e -> !submittedEmployeeIds.contains(e.getEmployeeId()))
                .map(e -> e.getName() + " - " + e.getEmployeeId())
                .collect(Collectors.toList());

        int totalPending = pendingEmployees.size();

        // -----------------------------
        // 6️⃣ Submitted list
        // -----------------------------
        List<String> submittedEmployees = employees.stream()
                .filter(e -> submittedEmployeeIds.contains(e.getEmployeeId()))
                .map(e -> e.getName() + " - " + e.getEmployeeId())
                .collect(Collectors.toList());

        // -----------------------------
        // 7️⃣ Department Stats
        // -----------------------------
        List<DepartmentStatsDTO> deptStats = deptRepo.findAll()
                .stream()
                .map(dept -> createDeptStats(surveyId, dept))
                .collect(Collectors.toList());

        if (departmentId != null) {
            deptStats = deptStats.stream()
                    .filter(d -> d.getDepartmentId().equals(departmentId))
                    .collect(Collectors.toList());
        }

        // -----------------------------
        // 8️⃣ Prepare DTO
        // -----------------------------
        DashboardResponseDTO dto = new DashboardResponseDTO();
        dto.setTotalEmployees(totalEmployees);
        dto.setTotalSubmitted(totalSubmitted);
        dto.setTotalPending(totalPending);
        dto.setTotalSurveys(surveyRepo.count());

        dto.setDepartmentStats(deptStats);
        dto.setSurveyStats(getSurveyStats(surveyId));
        dto.setSubmittedEmployees(submittedEmployees);
        dto.setPendingEmployees(pendingEmployees);

        return dto;
    }

    // -----------------------------
    // Create Department Stats
    // -----------------------------
    private DepartmentStatsDTO createDeptStats(Long surveyId, Department dept) {

        List<Employee> employees = employeeRepo.findByDepartmentId(dept.getId());
        int totalEmp = employees.size();

        List<String> submittedIds =
                (surveyId == null)
                        ? responseRepo.findAll().stream().map(SurveyResponse::getEmployeeId).toList()
                        : responseRepo.findBySurveyId(surveyId).stream().map(SurveyResponse::getEmployeeId).toList();

        long submittedCount = employees.stream()
                .filter(e -> submittedIds.contains(e.getEmployeeId()))
                .count();

        DepartmentStatsDTO dto = new DepartmentStatsDTO();
        dto.setDepartmentId(dept.getId());
        dto.setDepartmentName(dept.getName());
        dto.setTotalEmployees(totalEmp);
        dto.setSubmitted((int) submittedCount);
        dto.setPending(totalEmp - (int) submittedCount);
        dto.setResponseRate(totalEmp == 0 ? 0 : (100.0 * submittedCount / totalEmp));

        return dto;
    }

    // -----------------------------
    // OTHER INTERFACE METHODS
    // -----------------------------

    @Override
    public List<DepartmentStatsDTO> getDepartmentStats(Long surveyId, Long departmentId) {
        return getDashboardData(surveyId, departmentId).getDepartmentStats();
    }

    @Override
    public SurveyStatsDTO getSurveyStats(Long surveyId) {

        SurveyStatsDTO dto = new SurveyStatsDTO();

        if (surveyId == null) {
            dto.setSurveyId(0L);
            dto.setSurveyTitle("All Surveys");

            int total = employeeRepo.findAll().size();
            long submitted = responseRepo.count();

            dto.setTotalEmployees(total);
            dto.setTotalSubmitted(submitted);
            dto.setTotalPending(total - submitted);

            return dto;
        }

        Survey survey = surveyRepo.findById(surveyId).orElse(null);

        dto.setSurveyId(surveyId);
        dto.setSurveyTitle(survey != null ? survey.getTitle() : "Survey");

        int total = employeeRepo.findAll().size();
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
