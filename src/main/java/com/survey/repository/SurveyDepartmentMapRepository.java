package com.survey.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.survey.entity.SurveyDepartmentMap;

public interface SurveyDepartmentMapRepository extends JpaRepository<SurveyDepartmentMap, Long> {

	List<SurveyDepartmentMap> findBySurveyId(Long surveyId);

	void deleteAllBySurveyId(Long surveyId);

}
