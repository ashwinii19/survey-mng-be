package com.survey.repository;

import com.survey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    List<SurveyResponse> findBySurveyId(Long surveyId);

    List<SurveyResponse> findBySurveyIdAndEmployeeDepartmentId(Long surveyId, Long departmentId);

    List<SurveyResponse> findBySurveyIdAndSubmittedAtBetween(Long surveyId, LocalDateTime from, LocalDateTime to);

    List<SurveyResponse> findBySurveyIdAndEmployeeDepartmentIdAndSubmittedAtBetween(
            Long surveyId,
            Long departmentId,
            LocalDateTime from,
            LocalDateTime to
    );
}
