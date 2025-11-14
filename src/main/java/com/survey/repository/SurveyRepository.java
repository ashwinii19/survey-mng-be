package com.survey.repository;

import com.survey.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    Optional<Survey> findByTitle(String title);

    List<Survey> findAllByTitle(String title);
    
    List<Survey> findTop5ByOrderByPublishedAtDesc();
    
    

}
