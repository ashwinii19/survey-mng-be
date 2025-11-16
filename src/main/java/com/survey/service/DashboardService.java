package com.survey.service;

import com.survey.dto.dashboard.DashboardResponseDTO;
import com.survey.dto.dashboard.DepartmentStatsDTO;
import com.survey.dto.dashboard.SurveyStatsDTO;
import com.survey.entity.Department;
import com.survey.entity.Survey;

import java.util.List;

public interface DashboardService {

//    DashboardResponseDTO getDashboardData(Long surveyId, Long departmentId);
//
//    List<Survey> listSurveys();
//
//    List<Department> listDepartments();
//
//    List<DepartmentStatsDTO> getDepartmentStats(Long surveyId, Long departmentId);
//
//    SurveyStatsDTO getSurveyStats(Long surveyId);
//
//    List<String> listSubmittedEmployees(Long surveyId, Long departmentId);
//
//    List<String> listPendingEmployees(Long surveyId, Long departmentId);
//
//    List<Survey> recentSurveys(int limit);
	
	public DashboardResponseDTO getDashboardData(Long surveyId, Long departmentId);
	
	public List<DepartmentStatsDTO> getDepartmentStats(Long surveyId, Long departmentId);
	
	public SurveyStatsDTO getSurveyStats(Long surveyId);
	
	public List<String> listSubmittedEmployees(Long surveyId, Long departmentId);
	
	public List<String> listPendingEmployees(Long surveyId, Long departmentId);
	
	
}
