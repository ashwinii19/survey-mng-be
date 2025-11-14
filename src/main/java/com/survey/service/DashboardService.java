package com.survey.service;

import com.survey.dto.dashboard.DashboardResponseDTO;

public interface DashboardService {
	DashboardResponseDTO getDashboardData(Long surveyId, Long departmentId);
}
