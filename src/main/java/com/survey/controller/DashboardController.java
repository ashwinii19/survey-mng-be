package com.survey.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.survey.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/departments")
    public List<Map<String, Object>> getDepartments() {
        return dashboardService.getDepartments();
    }

    @GetMapping("/surveys")
    public List<Map<String, Object>> getSurveys() {
        return dashboardService.getSurveys();
    }

    @GetMapping("/employees")
    public List<Map<String, Object>> getEmployees(@RequestParam boolean submitted) {
        return dashboardService.getEmployees(submitted);
    }

    @GetMapping("/responseBreakdown/department")
    public List<Map<String, Object>> getResponseBreakdownByDepartment(@RequestParam(required = false) String department) {
        return dashboardService.getResponseBreakdownByDepartment(department);
    }

    @GetMapping("/responseBreakdown/survey")
    public List<Map<String, Object>> getResponseBreakdownBySurvey(@RequestParam(required = false) String survey) {
        return dashboardService.getResponseBreakdownBySurvey(survey);
    }
}
