package com.survey.repository;

import com.survey.entity.QuestionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionResponseRepository extends JpaRepository<QuestionResponse, Long> {

}
