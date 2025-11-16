package com.survey.service;

import java.util.List;

import com.survey.dto.request.SurveyRequestDTO;
import com.survey.dto.response.SurveyResponseDTO;

public interface SurveyService {
	SurveyResponseDTO createSurvey(SurveyRequestDTO dto);

	List<SurveyResponseDTO> getAllSurveys();

	SurveyResponseDTO getSurvey(Long id);

	SurveyResponseDTO publishSurvey(Long id);

	void deleteSurvey(Long id);
	
	public SurveyResponseDTO updateSurvey(Long id, SurveyRequestDTO dto);
}

