package com.survey.serviceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.survey.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSurveys", 8);
        summary.put("totalResponses", 520);
        summary.put("pendingResponses", 80);
        return summary;
    }

    @Override
    public List<Map<String, Object>> getDepartments() {
        return List.of(
            Map.of("department", "HR", "responseRate", 85),
            Map.of("department", "IT", "responseRate", 70),
            Map.of("department", "Finance", "responseRate", 90),
            Map.of("department", "Sales", "responseRate", 75)
        );
    }

    @Override
    public List<Map<String, Object>> getSurveys() {
        return List.of(
            Map.of("title", "Employee Satisfaction", "department", "HR", "responseRate", 82, "status", "Active"),
            Map.of("title", "Training Feedback", "department", "IT", "responseRate", 65, "status", "Completed"),
            Map.of("title", "Quarterly Review", "department", "Finance", "responseRate", 75, "status", "Active")
        );
    }

    @Override
    public List<Map<String, Object>> getEmployees(boolean submitted) {
        if (submitted) {
            return List.of(
                Map.of("name", "John Doe", "department", "HR"),
                Map.of("name", "Neha Patel", "department", "IT"),
                Map.of("name", "Ravi Kumar", "department", "Finance")
            );
        } else {
            return List.of(
                Map.of("name", "Amit Sharma", "department", "Sales"),
                Map.of("name", "Sara Ali", "department", "IT"),
                Map.of("name", "Priya Mehta", "department", "HR")
            );
        }
    }

    @Override
    public List<Map<String, Object>> getResponseBreakdownByDepartment(String dept) {
        // For real DB implementation, you can filter by department here
        if (dept != null && !dept.equalsIgnoreCase("All Departments")) {
            return List.of(
                Map.of("department", dept, "responseCount", 120)
            );
        }
        return List.of(
            Map.of("department", "HR", "responseCount", 120),
            Map.of("department", "IT", "responseCount", 90),
            Map.of("department", "Finance", "responseCount", 60),
            Map.of("department", "Sales", "responseCount", 80)
        );
    }

    @Override
    public List<Map<String, Object>> getResponseBreakdownBySurvey(String survey) {
        if (survey != null && !survey.equalsIgnoreCase("All Surveys")) {
            return List.of(
                Map.of("title", survey, "responseCount", 85)
            );
        }
        return List.of(
            Map.of("title", "Employee Satisfaction", "responseCount", 100),
            Map.of("title", "Exit Interview", "responseCount", 70),
            Map.of("title", "Annual Review", "responseCount", 50),
            Map.of("title", "Training Feedback", "responseCount", 95)
        );
    }
}
