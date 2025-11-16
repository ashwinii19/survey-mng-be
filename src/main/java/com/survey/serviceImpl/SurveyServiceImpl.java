package com.survey.serviceImpl;

import com.survey.dto.request.SurveyRequestDTO;
import com.survey.dto.response.SurveyResponseDTO;
import com.survey.entity.*;
import com.survey.repository.*;
import com.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
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

    @Value("${app.base-url}")
    private String baseUrl;


    // ======================================================================================
    // CREATE SURVEY
    // (DO NOT AUTO-PUBLISH)
    // ======================================================================================
    @Override
    public SurveyResponseDTO createSurvey(SurveyRequestDTO dto) {

        Survey survey = new Survey();
        survey.setTitle(dto.getTitle());
        survey.setDescription(dto.getDescription());

        /*
         * IMPORTANT:
         * Save-as-draft  → editable=true, published=false
         * Create-survey → editable=false, published=false
         * Publishing will be done separately by publishSurvey()
         */
        survey.setEditable(dto.isDraft());
        survey.setPublished(false);

        survey.setCreatedAt(LocalDateTime.now());

        if (dto.getTargetDepartmentName() != null) {
            Department dept = departmentRepository.findByName(dto.getTargetDepartmentName())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            survey.setTargetDepartment(dept);
        }

        // Final survey must have questions
        if (!dto.isDraft() && (dto.getQuestions() == null || dto.getQuestions().isEmpty())) {
            throw new RuntimeException("Final survey must contain at least one question");
        }

        Survey savedSurvey = surveyRepository.save(survey);

        // Form link
        String generatedLink = baseUrl + "/survey/" + savedSurvey.getId() + "/form";
        savedSurvey.setFormLink(generatedLink);
        surveyRepository.save(savedSurvey);

        // Save questions
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            List<Question> questions = dto.getQuestions().stream()
                    .map(qDto -> {
                        Question q = new Question();
                        q.setText(qDto.getText());
                        q.setQuestionType(qDto.getType().toUpperCase());
                        q.setOptions(String.join(",", qDto.getOptions()));
                        q.setRequired(qDto.isRequired());
                        q.setSurvey(savedSurvey);
                        return q;
                    }).collect(Collectors.toList());

            questionRepository.saveAll(questions);
        }

        return mapper.map(savedSurvey, SurveyResponseDTO.class);
    }


    // ======================================================================================
    // GET ALL SURVEYS
    // ======================================================================================
    @Override
    public List<SurveyResponseDTO> getAllSurveys() {
        return surveyRepository.findAll()
                .stream()
                .map(s -> mapper.map(s, SurveyResponseDTO.class))
                .collect(Collectors.toList());
    }


    // ======================================================================================
    // GET BY ID
    // ======================================================================================
    @Override
    public SurveyResponseDTO getSurvey(Long id) {
        Survey s = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found"));
        return mapper.map(s, SurveyResponseDTO.class);
    }


    // ======================================================================================
    // PUBLISH SURVEY
    // ======================================================================================
    @Override
    @Transactional
    public SurveyResponseDTO publishSurvey(Long id) {

        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        if (survey.isPublished()) {
            throw new RuntimeException("Survey is already published");
        }

        // Publish now
        survey.setEditable(false);
        survey.setPublished(true);
        survey.setPublishedAt(LocalDateTime.now());

        // Make sure DB immediately stores timestamp
        Survey savedSurvey = surveyRepository.saveAndFlush(survey);

        Department dept = survey.getTargetDepartment();

        List<Employee> employees = (dept != null)
                ? employeeRepository.findByDepartmentId(dept.getId())
                : employeeRepository.findAll();

        if (!employees.isEmpty()) {

            List<SurveyAssignment> assignments = employees.stream()
                    .map(emp -> SurveyAssignment.builder()
                            .survey(savedSurvey)
                            .department(dept != null ? dept : emp.getDepartment())
                            .employeeId(emp.getEmployeeId()) 
                            .assignedAt(LocalDateTime.now())
                            .dueDate(LocalDateTime.now().plusDays(7))
                            .reminderSent(false)
                            .build()
                    )
                    .collect(Collectors.toList());

            assignmentRepository.saveAll(assignments);

            // Email sending
            for (Employee emp : employees) {

                Context ctx = new Context();
                ctx.setVariable("employeeName", emp.getName());
                ctx.setVariable("surveyTitle", survey.getTitle());
                ctx.setVariable("dueDate", LocalDateTime.now().plusDays(7).toLocalDate().toString());

                String link = baseUrl + "/survey/" + savedSurvey.getId()
                        + "/form?employeeId=" + emp.getEmployeeId();
                ctx.setVariable("formLink", link);

                try {
                    emailService.sendEmailWithTemplate(
                            emp.getEmail(),
                            "New Survey Assigned: " + survey.getTitle(),
                            "survey-mail-template",
                            ctx
                    );
                } catch (Exception ex) {
                    System.err.println("Email error: " + ex.getMessage());
                }
            }
        }

        return mapper.map(savedSurvey, SurveyResponseDTO.class);
    }



    // ======================================================================================
    // DELETE SURVEY
    // ======================================================================================
    @Override
    @Transactional
    public void deleteSurvey(Long id) {

        // Step 1 — Delete all assignments linked with this survey
        assignmentRepository.deleteAllBySurveyId(id);

        // Step 2 — Delete all questions linked with this survey
        questionRepository.deleteAllBySurveyId(id);

        // Step 3 — Now safely delete the survey
        surveyRepository.deleteById(id);
    }



    // ======================================================================================
    // UPDATE SURVEY
    // ======================================================================================
    @Override
    @Transactional
    public SurveyResponseDTO updateSurvey(Long id, SurveyRequestDTO dto) {

        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        if (!survey.isEditable()) {
            throw new RuntimeException("Survey is not editable");
        }

        survey.setTitle(dto.getTitle());
        survey.setDescription(dto.getDescription());

        // CASE 1: Still a draft → editable=true
        if (dto.isDraft()) {
            survey.setEditable(true);
            survey.setPublished(false); 
        }

        // CASE 2: Final save (not draft) → editable=false
        else {
            survey.setEditable(false);
            survey.setPublished(false); // Will only publish after user clicks publish
        }

        if (dto.getTargetDepartmentName() != null) {
            Department dept = departmentRepository.findByName(dto.getTargetDepartmentName())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            survey.setTargetDepartment(dept);
        }

        // Remove old questions
        questionRepository.deleteAllBySurveyId(id);

        if (!dto.isDraft() && (dto.getQuestions() == null || dto.getQuestions().isEmpty())) {
            throw new RuntimeException("Final survey must contain at least one question");
        }

        // Save new questions
        if (dto.getQuestions() != null) {
            List<Question> questions = dto.getQuestions().stream()
                    .map(qDto -> {
                        Question q = new Question();
                        q.setText(qDto.getText());
                        q.setQuestionType(qDto.getType().toUpperCase());
                        q.setOptions(String.join(",", qDto.getOptions()));
                        q.setRequired(qDto.isRequired());
                        q.setSurvey(survey);
                        return q;
                    })
                    .collect(Collectors.toList());

            questionRepository.saveAll(questions);
        }

        Survey updated = surveyRepository.saveAndFlush(survey); // flush fix

        return mapper.map(updated, SurveyResponseDTO.class);
    }

}

