package com.survey.serviceImpl;

import com.survey.dto.request.SurveyRequestDTO;
import com.survey.dto.response.SurveyResponseDTO;
import com.survey.entity.*;
import com.survey.repository.*;
import com.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final SurveyAssignmentRepository assignmentRepository;
    private final QuestionRepository questionRepository;
    private final EmailService emailService;
    private final ModelMapper mapper;

    @Override
    public SurveyResponseDTO createSurvey(SurveyRequestDTO dto) {
        Survey survey = new Survey();
        survey.setTitle(dto.getTitle());
        survey.setDescription(dto.getDescription());
        survey.setPublished(false);
        survey.setCreatedAt(LocalDateTime.now());

        if (dto.getTargetDepartmentName() != null) {
            Department dept = departmentRepository.findByName(dto.getTargetDepartmentName())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            survey.setTargetDepartment(dept);
        }

        Survey savedSurvey = surveyRepository.save(survey);

        String generatedLink = "http://localhost:8080/survey/" + savedSurvey.getId() + "/form";
        savedSurvey.setFormLink(generatedLink);
        surveyRepository.save(savedSurvey);

        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            List<Question> questions = dto.getQuestions().stream()
                    .map(qDto -> {
                        Question q = mapper.map(qDto, Question.class);
                        q.setSurvey(savedSurvey);
                        return q;
                    }).collect(Collectors.toList());
            questionRepository.saveAll(questions);
            savedSurvey.setQuestions(questions);
        }

        return mapper.map(savedSurvey, SurveyResponseDTO.class);
    }

    @Override
    public List<SurveyResponseDTO> getAllSurveys() {
        return surveyRepository.findAll()
                .stream()
                .map(s -> mapper.map(s, SurveyResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public SurveyResponseDTO getSurvey(Long id) {
        Survey s = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found"));
        return mapper.map(s, SurveyResponseDTO.class);
    }

    @Override
    @Transactional
    public SurveyResponseDTO publishSurvey(Long id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        if (survey.isPublished()) {
            throw new RuntimeException("Survey is already published");
        }

        // Mark as published
        survey.setPublished(true);
        survey.setPublishedAt(LocalDateTime.now());
        Survey savedSurvey = surveyRepository.save(survey);

        Department dept = survey.getTargetDepartment();
        List<Employee> employees;

        if (dept != null) {
            System.out.println("Target Department ID: " + dept.getId());
            employees = employeeRepository.findByDepartmentId(dept.getId());
        } else {
            System.out.println("Global survey: assigning to all employees");
            employees = employeeRepository.findAll();
        }

        System.out.println("Employees found: " + employees.size());

        if (!employees.isEmpty()) {
            List<SurveyAssignment> assignments = employees.stream()
                    .map(emp -> SurveyAssignment.builder()
                            .survey(savedSurvey)
                            .department(dept != null ? dept : emp.getDepartment())
                            .employee(emp)
                            .assignedAt(LocalDateTime.now())
                            .dueDate(LocalDateTime.now().plusDays(7))
                            .reminderSent(false)
                            .build())
                    .collect(Collectors.toList());

            assignmentRepository.saveAll(assignments);
            System.out.println("Assignments saved: " + assignments.size());

            for (Employee emp : employees) {
                Context context = new Context();
                context.setVariable("employeeName", emp.getName());
                context.setVariable("surveyTitle", survey.getTitle());
                context.setVariable("dueDate", LocalDateTime.now().plusDays(7).toLocalDate().toString());

                String personalizedLink = "http://localhost:8080/survey/" + survey.getId() + "/form?employeeId=" + emp.getId();
                context.setVariable("formLink", personalizedLink);

                try {
                    emailService.sendEmailWithTemplate(
                            emp.getEmail(),
                            "New Survey Assigned: " + survey.getTitle(),
                            "survey-mail-template",
                            context
                    );
                    System.out.println("Email sent successfully to " + emp.getEmail());
                } catch (Exception e) {
                    System.err.println("Failed to send email to " + emp.getEmail() + ": " + e.getMessage());
                }
            }
        } else {
            System.out.println("No employees found to assign!");
        }

        return mapper.map(savedSurvey, SurveyResponseDTO.class);
    }

    @Override
    public void deleteSurvey(Long id) {
        surveyRepository.deleteById(id);
    }
}
