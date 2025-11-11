package com.survey.serviceImpl;

import com.survey.dto.request.*;
import com.survey.dto.response.*;
import com.survey.entity.*;
import com.survey.repository.*;
import com.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ModelMapper mapper;

    @Override
    public SurveyResponseDTO createSurvey(SurveyRequestDTO dto) {
        Survey survey = new Survey();
        survey.setTitle(dto.getTitle());
        survey.setDescription(dto.getDescription());

        // ✅ Always default to unpublished on creation
        survey.setPublished(false);
        survey.setCreatedAt(LocalDateTime.now());

        if (dto.getTargetDepartmentName() != null) {
            Department dept = departmentRepository.findByName(dto.getTargetDepartmentName())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            survey.setTargetDepartment(dept);
        }

        Survey savedSurvey = surveyRepository.save(survey);

        // ✅ Save questions if provided
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
        // 1️⃣ Fetch survey
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        if (survey.isPublished()) {
            throw new RuntimeException("Survey is already published");
        }

        // 2️⃣ Mark as published
        survey.setPublished(true);
        survey.setPublishedAt(LocalDateTime.now());
        Survey savedSurvey = surveyRepository.save(survey);

        // 3️⃣ Get target department
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

        // 4️⃣ Create assignments
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
        } else {
            System.out.println("⚠️ No employees found to assign!");
        }

        return mapper.map(savedSurvey, SurveyResponseDTO.class);
    }




    @Override
    public void deleteSurvey(Long id) {
        surveyRepository.deleteById(id);
    }
}

