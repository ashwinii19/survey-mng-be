package com.survey.repository;

import com.survey.entity.SurveyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SurveyAssignmentRepository extends JpaRepository<SurveyAssignment, Long> {

}
