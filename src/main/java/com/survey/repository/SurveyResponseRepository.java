package com.survey.repository;

import com.survey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    List<SurveyResponse> findBySurveyId(Long surveyId);

    List<SurveyResponse> findBySurveyIdAndSubmittedAtBetween(
            Long surveyId,
            LocalDateTime start,
            LocalDateTime end
    );

//    long countBy();

    List<SurveyResponse> findDistinctByEmployeeId(String employeeId);

    List<SurveyResponse> findBySurveyIdAndEmployeeIdIn(Long surveyId, List<String> employeeIds);

    List<SurveyResponse> findByEmployeeIdIn(List<String> employeeIds);

    long countBySurveyId(Long surveyId);

    long countByEmployeeId(String employeeId);
}
