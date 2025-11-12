package com.survey.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.survey.dto.request.ReminderRequestDTO;
import com.survey.dto.response.ReminderResponseDTO;
import com.survey.dto.response.ReminderSendResultDTO;
import com.survey.service.ReminderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/reminders")
@RequiredArgsConstructor
public class ReminderController {

	private final ReminderService reminderService;

	@PostMapping
	public ResponseEntity<ReminderResponseDTO> create(@RequestBody ReminderRequestDTO dto) {
		return ResponseEntity.ok(reminderService.createReminder(dto));
	}

	@GetMapping
	public ResponseEntity<List<ReminderResponseDTO>> list() {
		return ResponseEntity.ok(reminderService.listReminders());
	}

	@PostMapping("/{id}/send-now")
	public ResponseEntity<ReminderSendResultDTO> sendNow(@PathVariable Long id) {
		return ResponseEntity.ok(reminderService.sendReminderNow(id));
	}

	@PostMapping("/run-scheduler-now")
	public ResponseEntity<String> runSchedulerNow() {
		reminderService.processDueReminders();
		return ResponseEntity.ok("Reminder scheduler executed manually.");
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable Long id) {
		reminderService.deleteReminder(id);
		return ResponseEntity.ok("Reminder deleted successfully with ID: " + id);
	}
	
	@GetMapping("/{id}/submission-status")
	public ResponseEntity<Map<String, List<String>>> getSubmissionStatus(@PathVariable Long id) {
	    return ResponseEntity.ok(reminderService.getSubmissionStatus(id));
	}

}
