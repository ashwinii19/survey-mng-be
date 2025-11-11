package com.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.survey.entity.Survey;
import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    List<Survey> findAllByOrderByPublishedAtDesc();
}
