package com.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.survey.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {

}
