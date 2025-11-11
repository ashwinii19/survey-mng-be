package com.survey.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    Map<String, Object> getSummary();

    List<Map<String, Object>> getDepartments();

    List<Map<String, Object>> getSurveys();

    List<Map<String, Object>> getEmployees(boolean submitted);

    List<Map<String, Object>> getResponseBreakdownByDepartment(String dept);

    List<Map<String, Object>> getResponseBreakdownBySurvey(String survey);
}
