package com.survey.repository;

import com.survey.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
	List<Reminder> findBySurveyId(Long surveyId);

	List<Reminder> findByActiveTrue();

	List<Reminder> findBySentFalseAndScheduledAtBefore(LocalDateTime time);
}
