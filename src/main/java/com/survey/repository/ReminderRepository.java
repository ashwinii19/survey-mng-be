package com.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.survey.entity.Reminder;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
}
