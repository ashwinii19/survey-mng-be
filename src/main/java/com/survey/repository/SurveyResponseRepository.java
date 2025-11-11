package com.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.survey.entity.SurveyResponse;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
}
