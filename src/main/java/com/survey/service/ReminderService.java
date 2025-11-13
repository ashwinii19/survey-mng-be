package com.survey.service;

import com.survey.dto.request.ReminderRequestDTO;
import com.survey.dto.response.ReminderResponseDTO;
import com.survey.dto.response.ReminderSendResultDTO;

import java.util.List;
import java.util.Map;

public interface ReminderService {
	ReminderResponseDTO createReminder(ReminderRequestDTO dto);

	List<ReminderResponseDTO> listReminders();

	ReminderSendResultDTO sendReminderNow(Long reminderId);

	void processDueReminders();

	void deleteReminder(Long id);

	Map<String, List<String>> getSubmissionStatus(Long reminderId);
}
