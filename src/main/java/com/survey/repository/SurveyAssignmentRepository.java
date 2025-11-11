package com.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.survey.entity.SurveyAssignment;

public interface SurveyAssignmentRepository extends JpaRepository<SurveyAssignment, Long> {

}
