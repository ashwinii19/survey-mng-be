package com.survey.repository;

import com.survey.entity.SurveyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyAssignmentRepository extends JpaRepository<SurveyAssignment, Long> {

    Optional<SurveyAssignment> findBySurveyIdAndEmployeeId(Long surveyId, String employeeId);

    List<SurveyAssignment> findBySurveyId(Long surveyId);

    List<SurveyAssignment> findByEmployeeId(String employeeId);

    List<SurveyAssignment> findByDepartmentId(Long departmentId);
    
    void deleteAllBySurveyId(Long surveyId);

}
