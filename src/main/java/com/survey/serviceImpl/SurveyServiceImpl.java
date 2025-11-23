package com.survey.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import com.survey.dto.request.SurveyRequestDTO;
import com.survey.dto.response.QuestionResponseDTO;
import com.survey.dto.response.SurveyResponseDTO;
import com.survey.entity.Department;
import com.survey.entity.Employee;
import com.survey.entity.Question;
import com.survey.entity.Survey;
import com.survey.entity.SurveyAssignment;
import com.survey.entity.SurveyDepartmentMap;
import com.survey.repository.DepartmentRepository;
import com.survey.repository.EmployeeRepository;
import com.survey.repository.QuestionRepository;
import com.survey.repository.SurveyAssignmentRepository;
import com.survey.repository.SurveyDepartmentMapRepository;
import com.survey.repository.SurveyRepository;
import com.survey.service.SurveyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final SurveyAssignmentRepository assignmentRepository;
    private final QuestionRepository questionRepository;
    private final SurveyDepartmentMapRepository surveyDepartmentMapRepository;
    private final EmailService emailService;
    private final ModelMapper mapper;

    @Value("${app.base-url}")
    private String baseUrl;

    // ======================================================================================
    // CREATE SURVEY
    // ======================================================================================
    @Override
    public SurveyResponseDTO createSurvey(SurveyRequestDTO dto) {

        Survey survey = new Survey();
        survey.setTitle(dto.getTitle());
        survey.setDescription(dto.getDescription());
        survey.setEditable(dto.isDraft());
        survey.setPublished(false);
        survey.setCreatedAt(LocalDateTime.now());
        
        survey.setTargetPosition(dto.getTargetPosition());

        Survey savedSurvey = surveyRepository.save(survey);

        // ⭐ MULTIPLE DEPT SUPPORT
        createDepartmentMappingsForSurvey(dto.getTargetDepartments(), savedSurvey);

        if (!dto.isDraft() && (dto.getQuestions() == null || dto.getQuestions().isEmpty())) {
            throw new RuntimeException("Final survey must contain at least one question");
        }

        savedSurvey.setFormLink(baseUrl + "/survey/" + savedSurvey.getId() + "/form");
        surveyRepository.save(savedSurvey);

        // Save questions
        if (dto.getQuestions() != null) {
            List<Question> questions = dto.getQuestions().stream().map(qDto -> {
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

        return toDTO(savedSurvey);
    }

    // ======================================================================================
    // GET ALL SURVEYS
    // ======================================================================================
    @Override
    public List<SurveyResponseDTO> getAllSurveys() {
        return surveyRepository.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ======================================================================================
    // GET SURVEY BY ID
    // ======================================================================================
    @Override
    public SurveyResponseDTO getSurvey(Long id) {
        Survey s = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found"));
        return toDTO(s);
    }

    // ======================================================================================
    // PUBLISH SURVEY
    // ======================================================================================
//    @Override
//    @Transactional
//    public SurveyResponseDTO publishSurvey(Long id) {
//
//        Survey survey = surveyRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Survey not found"));
//
//        if (survey.isPublished()) {
//            throw new RuntimeException("Survey already published");
//        }
//
//        survey.setEditable(false);
//        survey.setPublished(true);
//        survey.setPublishedAt(LocalDateTime.now());
//
//        Survey savedSurvey = surveyRepository.saveAndFlush(survey);
//
//        List<SurveyDepartmentMap> mappings = surveyDepartmentMapRepository.findBySurveyId(savedSurvey.getId());
//
//        // No mapping means ALL departments
//        if (mappings == null || mappings.isEmpty()) {
//            List<Department> all = departmentRepository.findAll();
//            mappings = all.stream()
//                    .map(d -> SurveyDepartmentMap.builder().survey(savedSurvey).department(d).build())
//                    .collect(Collectors.toList());
//        }
//
//        // Assign employees
//        for (SurveyDepartmentMap map : mappings) {
//
//            Department dept = map.getDepartment();
//            List<Employee> employees = employeeRepository.findByDepartmentId(dept.getId());
//
//            if (employees.isEmpty()) continue;
//
//            List<SurveyAssignment> assignments = employees.stream()
//                    .map(emp -> SurveyAssignment.builder()
//                            .survey(savedSurvey)
//                            .department(dept)
//                            .employeeId(emp.getEmployeeId())
//                            .assignedAt(LocalDateTime.now())
//                            .dueDate(LocalDateTime.now().plusDays(7))
//                            .reminderSent(false)
//                            .build())
//                    .collect(Collectors.toList());
//
//            assignmentRepository.saveAll(assignments);
//
//            // Send Mail
//            for (Employee emp : employees) {
//                try {
//                    Context ctx = new Context();
//                    ctx.setVariable("employeeName", emp.getName());
//                    ctx.setVariable("surveyTitle", savedSurvey.getTitle());
//                    ctx.setVariable("dueDate", LocalDateTime.now().plusDays(7).toLocalDate().toString());
//                    ctx.setVariable("formLink", baseUrl + "/survey/" + savedSurvey.getId() + "/form?employeeId=" + emp.getEmployeeId());
//
//                    emailService.sendEmailWithTemplate(
//                            emp.getEmail(),
//                            "New Survey Assigned",
//                            "survey-mail-template",
//                            ctx
//                    );
//                } catch (Exception ignored) {}
//            }
//        }
//
//        return toDTO(savedSurvey);
//    }
    
    @Override
    @Transactional
    public SurveyResponseDTO publishSurvey(Long id) {

        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        if (survey.isPublished()) {
            throw new RuntimeException("Survey already published");
        }

        survey.setEditable(false);
        survey.setPublished(true);
        survey.setPublishedAt(LocalDateTime.now());
        Survey savedSurvey = surveyRepository.saveAndFlush(survey);

        List<SurveyDepartmentMap> mappings =
                surveyDepartmentMapRepository.findBySurveyId(savedSurvey.getId());

        String selectedPosition = survey.getTargetPosition();
        boolean hasPosition = selectedPosition != null && !selectedPosition.isBlank();

        boolean hasDepartments = mappings != null && !mappings.isEmpty();

        List<Employee> finalEmployees = new ArrayList<>();

        // CASE 1 — DEPARTMENT + POSITION
        if (hasDepartments && hasPosition) {

            for (SurveyDepartmentMap map : mappings) {
                List<Employee> emps = employeeRepository.findByDepartmentId(map.getDepartment().getId());
                finalEmployees.addAll(
                        emps.stream()
                                .filter(e -> selectedPosition.equalsIgnoreCase(e.getPosition()))
                                .collect(Collectors.toList())
                );
            }

        }
        // CASE 2 — ONLY POSITION
        else if (!hasDepartments && hasPosition) {

            finalEmployees = employeeRepository.findAll().stream()
                    .filter(e -> selectedPosition.equalsIgnoreCase(e.getPosition()))
                    .collect(Collectors.toList());

        }
        // CASE 3 — ONLY DEPARTMENTS (NORMAL)
        else if (hasDepartments && !hasPosition) {

            for (SurveyDepartmentMap map : mappings) {
                finalEmployees.addAll(employeeRepository.findByDepartmentId(map.getDepartment().getId()));
            }

        }
        // CASE 4 — Send to ALL employees
        else {
            finalEmployees = employeeRepository.findAll();
        }

        // ====== ASSIGN SURVEY + SEND EMAIL ======
        for (Employee emp : finalEmployees) {

            SurveyAssignment assign = SurveyAssignment.builder()
                    .survey(savedSurvey)
                    .department(emp.getDepartment())
                    .employeeId(emp.getEmployeeId())
                    .assignedAt(LocalDateTime.now())
                    .dueDate(LocalDateTime.now().plusDays(7))
                    .reminderSent(false)
                    .build();

            assignmentRepository.save(assign);

            try {
                Context ctx = new Context();
                ctx.setVariable("employeeName", emp.getName());
                ctx.setVariable("surveyTitle", savedSurvey.getTitle());
                ctx.setVariable("dueDate", LocalDateTime.now().plusDays(7).toLocalDate().toString());
                ctx.setVariable("formLink",
                        baseUrl + "/survey/" + savedSurvey.getId() + "/form?employeeId=" + emp.getEmployeeId());

                emailService.sendEmailWithTemplate(
                        emp.getEmail(),
                        "New Survey Assigned",
                        "survey-mail-template",
                        ctx
                );
            } catch (Exception ignored) {}
        }

        return toDTO(savedSurvey);
    }


    // ======================================================================================
    // DELETE SURVEY
    // ======================================================================================
    @Override
    public void deleteSurvey(Long id) {
        assignmentRepository.deleteAllBySurveyId(id);
        questionRepository.deleteAllBySurveyId(id);
        surveyDepartmentMapRepository.deleteAllBySurveyId(id);
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
        survey.setEditable(dto.isDraft());
        survey.setPublished(false);
        survey.setTargetPosition(dto.getTargetPosition());

        questionRepository.deleteAllBySurveyId(id);

        if (!dto.isDraft() && (dto.getQuestions() == null || dto.getQuestions().isEmpty())) {
            throw new RuntimeException("Final survey must have at least one question");
        }

        // Add Questions
        if (dto.getQuestions() != null) {
            List<Question> questions = dto.getQuestions().stream().map(qDto -> {
                Question q = new Question();
                q.setText(qDto.getText());
                q.setQuestionType(qDto.getType().toUpperCase());
                q.setOptions(String.join(",", qDto.getOptions()));
                q.setRequired(qDto.isRequired());
                q.setSurvey(survey);
                return q;
            }).collect(Collectors.toList());
            questionRepository.saveAll(questions);
        }

        Survey updated = surveyRepository.saveAndFlush(survey);

        // ⭐ MULTI DEPARTMENT
        createDepartmentMappingsForSurvey(dto.getTargetDepartments(), updated);

        return toDTO(updated);
    }

    // ======================================================================================
    // CREATE MULTIPLE MAPPINGS
    // ======================================================================================
    private void createDepartmentMappingsForSurvey(List<Long> deptIds, Survey survey) {

        surveyDepartmentMapRepository.deleteAllBySurveyId(survey.getId());

        List<SurveyDepartmentMap> mappings = new ArrayList<>();

        if (deptIds == null || deptIds.isEmpty()) {
            // ALL departments
            List<Department> all = departmentRepository.findAll();
            for (Department d : all) {
                mappings.add(SurveyDepartmentMap.builder().survey(survey).department(d).build());
            }
        } else {
            List<Department> deptList = departmentRepository.findAllById(deptIds);
            for (Department d : deptList) {
                mappings.add(SurveyDepartmentMap.builder().survey(survey).department(d).build());
            }
        }

        surveyDepartmentMapRepository.saveAll(mappings);
    }

    // ======================================================================================
    // DTO MAPPING
    // ======================================================================================
//    private SurveyResponseDTO toDTO(Survey survey) {
//
//        SurveyResponseDTO dto = new SurveyResponseDTO();
//
//        dto.setId(survey.getId());
//        dto.setTitle(survey.getTitle());
//        dto.setDescription(survey.getDescription());
//        dto.setPublished(survey.isPublished());
//        dto.setEditable(survey.isEditable());
//        dto.setFormLink(survey.getFormLink());
//        dto.setCreatedAt(survey.getCreatedAt());
//        dto.setPublishedAt(survey.getPublishedAt());
//
//        // -------- QUESTIONS --------
//        dto.setQuestions(
//                survey.getQuestions().stream()
//                        .map(q -> {
//                            QuestionResponseDTO qDto = new QuestionResponseDTO();
//                            qDto.setId(q.getId());
//                            qDto.setText(q.getText());
//                            qDto.setType(q.getQuestionType());
//                            qDto.setOptions(List.of(q.getOptions().split(",")));
//                            qDto.setRequired(q.isRequired());
//                            return qDto;
//                        })
//                        .collect(Collectors.toList())
//        );
//
//        // -------- DEPARTMENTS --------
//        List<SurveyDepartmentMap> maps = surveyDepartmentMapRepository.findBySurveyId(survey.getId());
//        long totalDept = departmentRepository.count();
//
//        List<String> deptNames;
//
//        if (maps.isEmpty() || maps.size() == totalDept) {
//            deptNames = List.of("ALL");
//        } else {
//            deptNames = maps.stream()
//                    .map(m -> m.getDepartment().getName())
//                    .collect(Collectors.toList());
//        }
//
//        dto.setTargetDepartments(deptNames);
//        dto.setTargetDepartmentName(String.join(", ", deptNames));
//
//        return dto;
//    }
    
    private SurveyResponseDTO toDTO(Survey survey) {

        SurveyResponseDTO dto = new SurveyResponseDTO();

        dto.setId(survey.getId());
        dto.setTitle(survey.getTitle());
        dto.setDescription(survey.getDescription());
        dto.setPublished(survey.isPublished());
        dto.setEditable(survey.isEditable());
        dto.setFormLink(survey.getFormLink());
        dto.setCreatedAt(survey.getCreatedAt());
        dto.setPublishedAt(survey.getPublishedAt());
        dto.setTargetPosition(survey.getTargetPosition());

        // Questions
        dto.setQuestions(
                survey.getQuestions().stream()
                        .map(q -> {
                            QuestionResponseDTO qDto = new QuestionResponseDTO();
                            qDto.setId(q.getId());
                            qDto.setText(q.getText());
                            qDto.setType(q.getQuestionType());
                            qDto.setOptions(List.of(q.getOptions().split(",")));
                            qDto.setRequired(q.isRequired());
                            return qDto;
                        })
                        .collect(Collectors.toList())
        );

        // Departments
        List<SurveyDepartmentMap> maps = surveyDepartmentMapRepository.findBySurveyId(survey.getId());
        long totalDept = departmentRepository.count();

        List<String> deptNames;

        if (maps.isEmpty() || maps.size() == totalDept) {
            deptNames = List.of("ALL");
        } else {
            deptNames = maps.stream()
                    .map(m -> m.getDepartment().getName())
                    .collect(Collectors.toList());
        }

        dto.setTargetDepartments(deptNames);
        dto.setTargetDepartmentName(String.join(", ", deptNames));

        return dto;
    }
}
