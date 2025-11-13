package com.survey.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.survey.dto.request.ReminderRequestDTO;
import com.survey.dto.response.ReminderResponseDTO;
import com.survey.dto.response.ReminderSendResultDTO;
import com.survey.entity.Department;
import com.survey.entity.Employee;
import com.survey.entity.Reminder;
import com.survey.entity.Survey;
import com.survey.entity.SurveyAssignment;
import com.survey.repository.DepartmentRepository;
import com.survey.repository.EmployeeRepository;
import com.survey.repository.ReminderRepository;
import com.survey.repository.SurveyAssignmentRepository;
import com.survey.repository.SurveyRepository;
import com.survey.repository.SurveyResponseRepository;
import com.survey.service.ReminderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository reminderRepository;
    private final SurveyRepository surveyRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyAssignmentRepository assignmentRepository;
    private final EmailService emailService;
    private final ModelMapper mapper;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public ReminderResponseDTO createReminder(ReminderRequestDTO dto) {
        List<Survey> surveys = surveyRepository.findAllByTitle(dto.getSurveyName());
        if (surveys.isEmpty()) {
            throw new RuntimeException("Survey not found with title: " + dto.getSurveyName());
        }

        Survey survey = surveys.stream()
                .max(Comparator.comparing(Survey::getCreatedAt))
                .orElseThrow(() -> new RuntimeException("No valid survey found."));

        Department department = null;
        if (dto.getDepartmentName() != null && !dto.getDepartmentName().isEmpty()) {
            department = departmentRepository.findByName(dto.getDepartmentName())
                    .orElseThrow(() -> new RuntimeException("Department not found: " + dto.getDepartmentName()));
        }

        Reminder reminder = Reminder.builder()
                .message(dto.getMessage())
                .scheduledAt(dto.getScheduledAt())
                .nextScheduledAt(dto.getScheduledAt())
                .intervalInDays(dto.getIntervalInDays())
                .active(true)
                .sent(false)
                .survey(survey)
                .department(department)
                .build();

        Reminder saved = reminderRepository.save(reminder);

        ReminderResponseDTO response = mapper.map(saved, ReminderResponseDTO.class);
        response.setSurveyTitle(survey.getTitle());
        response.setDepartmentName(department != null ? department.getName() : "All Departments");
        response.setStatus("Pending");
        response.setNextScheduledAt(saved.getNextScheduledAt());
        return response;
    }

    @Override
    public List<ReminderResponseDTO> listReminders() {
        return reminderRepository.findAll().stream().map(reminder -> {
            ReminderResponseDTO dto = mapper.map(reminder, ReminderResponseDTO.class);
            dto.setSurveyTitle(reminder.getSurvey() != null ? reminder.getSurvey().getTitle() : null);
            dto.setDepartmentName(reminder.getDepartment() != null ? reminder.getDepartment().getName() : "All Departments");
            dto.setStatus(reminder.isSent() ? "Completed" : "Pending");
            dto.setNextScheduledAt(reminder.getNextScheduledAt());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ReminderSendResultDTO sendReminderNow(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));

        ReminderSendResultDTO result = processReminder(reminder, true);

        reminder.setSent(true);
        reminder.setSentAt(LocalDateTime.now());

        if (reminder.getIntervalInDays() != null && reminder.getIntervalInDays() > 0) {
            LocalDateTime next = reminder.getScheduledAt().plusDays(reminder.getIntervalInDays());
            reminder.setNextScheduledAt(next);
            reminder.setScheduledAt(next);
        }

        reminderRepository.save(reminder);
        return result;
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void scheduledReminderRunner() {
        processDueReminders();
    }

    @Override
    public void processDueReminders() {
        List<Reminder> dueReminders = reminderRepository.findAll().stream()
                .filter(r -> r.isActive() && r.getScheduledAt() != null && !r.getScheduledAt().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        for (Reminder reminder : dueReminders) {
            try {
                processReminder(reminder, false);
                reminder.setSent(true);
                reminder.setSentAt(LocalDateTime.now());

                if (reminder.getIntervalInDays() != null && reminder.getIntervalInDays() > 0) {
                    LocalDateTime next = reminder.getScheduledAt().plusDays(reminder.getIntervalInDays());
                    reminder.setNextScheduledAt(next);
                    reminder.setScheduledAt(next);
                }

                reminderRepository.save(reminder);
            } catch (Exception e) {
                // swallow so other reminders still run
            }
        }
    }

    private ReminderSendResultDTO processReminder(Reminder reminder, boolean sendToAll) {
        Survey survey = reminder.getSurvey();
        Department department = reminder.getDepartment();

        List<Employee> employees = (department != null)
                ? employeeRepository.findByDepartmentId(department.getId())
                : employeeRepository.findAll();

        Set<String> submittedEmployeeIds = responseRepository.findBySurveyId(survey.getId())
                .stream()
                .map(r -> r.getEmployeeId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Employee> recipients = employees.stream()
                .filter(emp -> sendToAll || !submittedEmployeeIds.contains(emp.getEmployeeId()))
                .collect(Collectors.toList());

        List<String> sentTo = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (Employee emp : recipients) {
            try {
                Context context = new Context();
                context.setVariable("employeeName", emp.getName());
                context.setVariable("surveyTitle", survey.getTitle());
                String link = baseUrl + "/survey/" + survey.getId() + "/form?employeeId=" + emp.getEmployeeId();
                context.setVariable("formLink", link);
                context.setVariable("message", reminder.getMessage());
                context.setVariable("dueDate", reminder.getScheduledAt() != null ? reminder.getScheduledAt().toLocalDate().toString() : "");

                emailService.sendEmailWithTemplate(emp.getEmail(),
                        "Survey Reminder: " + survey.getTitle(),
                        "survey-mail-template", context);
                sentTo.add(emp.getEmail());

                SurveyAssignment assignment = assignmentRepository.findBySurveyIdAndEmployeeId(
                        survey.getId(), emp.getEmployeeId()).orElse(null);
                if (assignment != null) {
                    assignment.setReminderSent(true);
                    assignmentRepository.save(assignment);
                }

            } catch (MailException e) {
                failed.add(emp.getEmail());
            }
        }

        ReminderSendResultDTO result = new ReminderSendResultDTO();
        result.setReminderId(reminder.getId());
        result.setSurveyTitle(survey.getTitle());
        result.setDepartmentName(department != null ? department.getName() : "All Departments");
        result.setTotalEmployees(employees.size());
        result.setPendingCount((int) employees.stream().filter(emp -> !submittedEmployeeIds.contains(emp.getEmployeeId())).count());
        result.setSentTo(sentTo);
        result.setFailed(failed);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    @Override
    public Map<String, List<String>> getSubmissionStatus(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));
        Survey survey = reminder.getSurvey();
        Department department = reminder.getDepartment();

        List<Employee> employees = (department != null)
                ? employeeRepository.findByDepartmentId(department.getId())
                : employee_repository_find_all();

        Set<String> submittedEmployeeIds = response_repository_find_by_surveyId_collect_employeeIds(survey.getId());

        List<String> submitted = employees.stream()
                .filter(emp -> submittedEmployeeIds.contains(emp.getEmployeeId()))
                .map(Employee::getName)
                .collect(Collectors.toList());

        List<String> notSubmitted = employees.stream()
                .filter(emp -> !submittedEmployeeIds.contains(emp.getEmployeeId()))
                .map(Employee::getName)
                .collect(Collectors.toList());

        Map<String, List<String>> result = new HashMap<>();
        result.put("submitted", submitted);
        result.put("notSubmitted", notSubmitted);
        return result;
    }

    private List<Employee> employee_repository_find_all() {
        return employeeRepository.findAll();
    }

    private Set<String> response_repository_find_by_surveyId_collect_employeeIds(Long surveyId) {
        return responseRepository.findBySurveyId(surveyId)
                .stream()
                .map(r -> r.getEmployeeId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public void deleteReminder(Long id) {
        reminderRepository.deleteById(id);
    }
}
