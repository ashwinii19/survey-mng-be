//package com.survey.controller;
//
//import java.util.List;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.survey.dto.dashboard.DashboardResponseDTO;
//import com.survey.dto.dashboard.DepartmentStatsDTO;
//import com.survey.dto.dashboard.SurveyStatsDTO;
//import com.survey.entity.Department;
//import com.survey.entity.Survey;
//import com.survey.service.DashboardService;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequestMapping("/api/admin/dashboard")
//@RequiredArgsConstructor
//public class DashboardController {
//
//    private final DashboardService dashboardService;
//
//    // main dashboard (supports optional filters)
//    @GetMapping
//    public DashboardResponseDTO getDashboard(
//            @RequestParam(required = false) Long surveyId,
//            @RequestParam(required = false) Long departmentId
//    ) {
//        return dashboardService.getDashboardData(surveyId, departmentId);
//    }
//
//    @GetMapping("/surveys")
//    public List<Survey> getSurveys() {
//        return dashboardService.listSurveys();
//    }
//
//    @GetMapping("/departments")
//    public List<Department> getDepartments() {
//        return dashboardService.listDepartments();
//    }
//
//    @GetMapping("/recent-surveys")
//    public List<Survey> getRecentSurveys(@RequestParam(defaultValue = "5") int limit) {
//        return dashboardService.recentSurveys(limit);
//    }
//
//    @GetMapping("/department-stats")
//    public List<DepartmentStatsDTO> getDepartmentStats(
//            @RequestParam(required = false) Long surveyId,
//            @RequestParam(required = false) Long departmentId
//    ) {
//        return dashboardService.getDepartmentStats(surveyId, departmentId);
//    }
//
//    @GetMapping("/survey/{id}/stats")
//    public SurveyStatsDTO getSurveyStats(@PathVariable("id") Long id) {
//        return dashboardService.getSurveyStats(id);
//    }
//
//    @GetMapping("/submitted")
//    public List<String> getSubmitted(
//            @RequestParam(required = false) Long surveyId,
//            @RequestParam(required = false) Long departmentId
//    ) {
//        return dashboardService.listSubmittedEmployees(surveyId, departmentId);
//    }
//
//    @GetMapping("/pending")
//    public List<String> getPending(
//            @RequestParam(required = false) Long surveyId,
//            @RequestParam(required = false) Long departmentId
//    ) {
//        return dashboardService.listPendingEmployees(surveyId, departmentId);
//    }
//}

package com.survey.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.survey.dto.dashboard.DashboardResponseDTO;
import com.survey.dto.dashboard.DepartmentStatsDTO;
import com.survey.dto.dashboard.SurveyStatsDTO;
import com.survey.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** MAIN DASHBOARD DATA */
    @GetMapping
    public DashboardResponseDTO getDashboard(
            @RequestParam(required = false) Long surveyId,
            @RequestParam(required = false) Long departmentId
    ) {
        return dashboardService.getDashboardData(surveyId, departmentId);
    }

    /** DEPARTMENT STATS */
    @GetMapping("/department-stats")
    public List<DepartmentStatsDTO> getDepartmentStats(
            @RequestParam(required = false) Long surveyId,
            @RequestParam(required = false) Long departmentId
    ) {
        return dashboardService.getDepartmentStats(surveyId, departmentId);
    }

    /** SURVEY STATS */
    @GetMapping("/survey/{id}/stats")
    public SurveyStatsDTO getSurveyStats(@PathVariable Long id) {
        return dashboardService.getSurveyStats(id);
    }

    @GetMapping("/submitted")
    public List<String> getSubmitted(
            @RequestParam(required = false) Long surveyId,
            @RequestParam(required = false) Long departmentId
    ) {
        return dashboardService.listSubmittedEmployees(surveyId, departmentId);
    }

    @GetMapping("/pending")
    public List<String> getPending(
            @RequestParam(required = false) Long surveyId,
            @RequestParam(required = false) Long departmentId
    ) {
        return dashboardService.listPendingEmployees(surveyId, departmentId);
    }
}

