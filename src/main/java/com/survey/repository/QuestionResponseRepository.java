package com.survey.repository;

import com.survey.entity.QuestionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionResponseRepository extends JpaRepository<QuestionResponse, Long> {

    List<QuestionResponse> findByQuestion_Id(Long questionId);

    List<QuestionResponse> findBySurveyResponse_Survey_Id(Long surveyId);

    List<QuestionResponse> findBySurveyResponse_Survey_IdAndQuestion_Id(Long surveyId, Long questionId);
}
