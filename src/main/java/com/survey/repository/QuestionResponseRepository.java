package com.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.survey.entity.QuestionResponse;

public interface QuestionResponseRepository extends JpaRepository<QuestionResponse, Long> {
}
