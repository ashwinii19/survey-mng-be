package com.survey.service;

import java.time.LocalDate;
import java.util.List;

import com.survey.dto.response.QuestionAnswerResponseDTO;
import com.survey.dto.response.QuestionStatsDTO;
import com.survey.dto.response.SurveySubmissionResponseDTO;

public interface SurveyResponseService {
    List<SurveySubmissionResponseDTO> getAllSurveyResponsesSummary();

    SurveySubmissionResponseDTO getSurveyResponseSummary(Long surveyId);

    SurveySubmissionResponseDTO getSurveyResponseSummaryByDept(Long surveyId, Long departmentId);

    SurveySubmissionResponseDTO getFilteredSurveyResponses(Long surveyId, Long departmentId, String employeeName,
                                                           LocalDate fromDate, LocalDate toDate);
    
    List<QuestionStatsDTO> getQuestionStatsForSurvey(Long surveyId);

    List<QuestionAnswerResponseDTO> getAnswersForQuestion(Long surveyId, Long questionId);
    
}
