package com.survey.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.survey.dto.response.QuestionAnswerResponseDTO;
import com.survey.dto.response.QuestionStatsDTO;
import com.survey.dto.response.SurveySubmissionResponseDTO;
import com.survey.entity.Department;
import com.survey.entity.Employee;
import com.survey.entity.Question;
import com.survey.entity.Survey;
import com.survey.entity.SurveyDepartmentMap;
import com.survey.entity.SurveyResponse;
import com.survey.entity.QuestionResponse;
import com.survey.repository.DepartmentRepository;
import com.survey.repository.EmployeeRepository;
import com.survey.repository.QuestionRepository;
import com.survey.repository.SurveyAssignmentRepository;
import com.survey.repository.SurveyDepartmentMapRepository;
import com.survey.repository.SurveyRepository;
import com.survey.repository.SurveyResponseRepository;
import com.survey.repository.QuestionResponseRepository;
import com.survey.service.SurveyResponseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyResponseServiceImpl implements SurveyResponseService {

    private final SurveyRepository surveyRepository;
    private final EmployeeRepository employeeRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final SurveyAssignmentRepository assignmentRepository;
    private final DepartmentRepository departmentRepository;
    private final SurveyDepartmentMapRepository surveyDepartmentMapRepository;
    private final QuestionRepository questionRepository;
    private final QuestionResponseRepository questionResponseRepository;

    // ========================= existing summary methods (kept as-is) =========================
    @Override
    public List<SurveySubmissionResponseDTO> getAllSurveyResponsesSummary() {
        return surveyRepository.findAll()
                .stream()
                .map(this::buildSurveySummary)
                .collect(Collectors.toList());
    }

    @Override
    public SurveySubmissionResponseDTO getSurveyResponseSummary(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));
        return buildSurveySummary(survey);
    }

    @Override
    public SurveySubmissionResponseDTO getSurveyResponseSummaryByDept(Long surveyId, Long departmentId) {
        return getFilteredSurveyResponses(surveyId, departmentId, null, null, null);
    }

    @Override
    public SurveySubmissionResponseDTO getFilteredSurveyResponses(Long surveyId, Long departmentId, String employeeName,
                                                                  LocalDate fromDate, LocalDate toDate) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        List<SurveyDepartmentMap> mappings = surveyDepartmentMapRepository.findBySurveyId(surveyId);
        long totalDeptCount = departmentRepository.count();

        boolean isAllDeptSurvey = (mappings.isEmpty() || mappings.size() == totalDeptCount);

        Department selectedDept = null;

        if (departmentId != null) {
            selectedDept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found"));

            if (!isAllDeptSurvey) {
                boolean allowed = mappings.stream()
                        .anyMatch(m -> m.getDepartment().getId().equals(departmentId));

                if (!allowed) {
                    throw new RuntimeException(
                            "Survey is NOT assigned to department: " + selectedDept.getName()
                    );
                }
            }
        }

        // ---------------- FETCH EMPLOYEES ----------------
        List<Employee> employees;

        if (selectedDept != null) {
            employees = employeeRepository.findByDepartmentId(selectedDept.getId());
        } else {
            // if survey is not ALL and mappings exist, use those departments employees only
            if (!isAllDeptSurvey && (mappings != null && !mappings.isEmpty())) {
                Set<Long> deptIds = mappings.stream()
                        .map(m -> m.getDepartment().getId())
                        .collect(Collectors.toSet());
                employees = employeeRepository.findAll().stream()
                        .filter(e -> e.getDepartment() != null && deptIds.contains(e.getDepartment().getId()))
                        .collect(Collectors.toList());
            } else {
                employees = employeeRepository.findAll();
            }
        }

        // ---------------- FETCH RESPONSES ----------------
        List<SurveyResponse> responses;

        if (fromDate != null && toDate != null) {
            responses = surveyResponseRepository.findBySurveyIdAndSubmittedAtBetween(
                    surveyId,
                    fromDate.atStartOfDay(),
                    toDate.atTime(23, 59)
            );
        } else {
            responses = surveyResponseRepository.findBySurveyId(surveyId);
        }

        Set<String> allowedEmpIds = employees.stream()
                .map(Employee::getEmployeeId)
                .collect(Collectors.toSet());

        List<SurveyResponse> filteredResponses = responses.stream()
                .filter(r -> allowedEmpIds.contains(r.getEmployeeId()))
                .collect(Collectors.toList());

        // -------- Submitted --------
        List<String> submitted = filteredResponses.stream()
                .map(SurveyResponse::getEmployeeId)
                .map(id -> employees.stream()
                        .filter(e -> e.getEmployeeId().equals(id))
                        .map(Employee::getName)
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .filter(name -> employeeName == null ||
                        name.toLowerCase().contains(employeeName.toLowerCase()))
                .collect(Collectors.toList());

        // -------- Pending --------
        List<String> pending = employees.stream()
                .map(Employee::getName)
                .filter(name -> !submitted.contains(name))
                .filter(name -> employeeName == null ||
                        name.toLowerCase().contains(employeeName.toLowerCase()))
                .collect(Collectors.toList());

        // ---------------- BUILD DTO ----------------
        SurveySubmissionResponseDTO dto = new SurveySubmissionResponseDTO();

        dto.setSurveyId(survey.getId());
        dto.setSurveyTitle(survey.getTitle());
        dto.setDepartmentName(selectedDept != null ? selectedDept.getName() : "ALL");

        dto.setTotalEmployees(employees.size());
        dto.setSubmittedCount(submitted.size());
        dto.setPendingCount(pending.size());
        dto.setSubmittedEmployees(submitted);
        dto.setPendingEmployees(pending);

        return dto;
    }

    @Transactional
    public void submitSurvey(Long surveyId, String employeeId, List<com.survey.entity.QuestionResponse> answers) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);
        response.setEmployeeId(employee.getEmployeeId());
        response.setSubmittedAt(LocalDateTime.now());

        answers.forEach(a -> a.setSurveyResponse(response));
        response.setQuestionResponses(answers);

        surveyResponseRepository.save(response);

        assignmentRepository.findBySurveyIdAndEmployeeId(surveyId, employee.getEmployeeId())
                .ifPresent(a -> {
                    a.setReminderSent(true);
                    assignmentRepository.save(a);
                });
    }

    // ========================= helper used by admin summary page =========================
//    private SurveySubmissionResponseDTO buildSurveySummary(Survey survey) {
//
//        List<SurveyDepartmentMap> mappings = surveyDepartmentMapRepository.findBySurveyId(survey.getId());
//        long totalDeptCount = departmentRepository.count();
//
//        List<Department> depts;
//
//        if (mappings.isEmpty() || mappings.size() == totalDeptCount) {
//            depts = departmentRepository.findAll();
//        } else {
//            depts = mappings.stream()
//                    .map(SurveyDepartmentMap::getDepartment)
//                    .collect(Collectors.toList());
//        }
//
//        List<Employee> employees = new ArrayList<>();
//        for (Department d : depts) {
//            employees.addAll(employeeRepository.findByDepartmentId(d.getId()));
//        }
//
//        List<SurveyResponse> responses = surveyResponseRepository.findBySurveyId(survey.getId());
//
//        List<String> submitted = responses.stream()
//                .map(r -> employeeRepository.findByEmployeeId(r.getEmployeeId())
//                        .map(Employee::getName).orElse(null))
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//
//        List<String> pending = employees.stream()
//                .map(Employee::getName)
//                .filter(name -> !submitted.contains(name))
//                .collect(Collectors.toList());
//
//        SurveySubmissionResponseDTO dto = new SurveySubmissionResponseDTO();
//
//        dto.setSurveyId(survey.getId());
//        dto.setSurveyTitle(survey.getTitle());
//
//        dto.setDepartmentName(
//                mappings.size() == totalDeptCount
//                        ? "ALL"
//                        : depts.stream().map(Department::getName).collect(Collectors.joining(", "))
//        );
//
//        dto.setTotalEmployees(employees.size());
//        dto.setSubmittedCount(submitted.size());
//        dto.setPendingCount(pending.size());
//        dto.setSubmittedEmployees(submitted);
//        dto.setPendingEmployees(pending);
//
//        return dto;
//    }
    
    private SurveySubmissionResponseDTO buildSurveySummary(Survey survey) {

        // ✅ Fetch assigned employees from SurveyAssignment table
        List<String> assignedEmpIds = assignmentRepository.findBySurveyId(survey.getId())
                .stream()
                .map(a -> a.getEmployeeId())
                .collect(Collectors.toList());

        List<Employee> employees = assignedEmpIds.isEmpty()
                ? List.of()
                : employeeRepository.findByEmployeeIdIn(assignedEmpIds);

        // ✅ Submitted responses
        List<SurveyResponse> responses = surveyResponseRepository.findBySurveyId(survey.getId());

        Set<String> submittedIds = responses.stream()
                .map(SurveyResponse::getEmployeeId)
                .collect(Collectors.toSet());

        List<String> submitted = employees.stream()
                .filter(e -> submittedIds.contains(e.getEmployeeId()))
                .map(Employee::getName)
                .collect(Collectors.toList());

        // ✅ Pending = assigned but not submitted
        List<String> pending = employees.stream()
                .filter(e -> !submittedIds.contains(e.getEmployeeId()))
                .map(Employee::getName)
                .collect(Collectors.toList());

        SurveySubmissionResponseDTO dto = new SurveySubmissionResponseDTO();
        dto.setSurveyId(survey.getId());
        dto.setSurveyTitle(survey.getTitle());
        dto.setDepartmentName("Assigned Departments");

        dto.setTotalEmployees(employees.size());
        dto.setSubmittedCount(submitted.size());
        dto.setPendingCount(pending.size());
        dto.setSubmittedEmployees(submitted);
        dto.setPendingEmployees(pending);

        return dto;
    }


    // ========================= NEW: question-wise statistics for a survey =========================
//    @Override
//    public List<QuestionStatsDTO> getQuestionStatsForSurvey(Long surveyId) {
//        Survey survey = surveyRepository.findById(surveyId)
//                .orElseThrow(() -> new RuntimeException("Survey not found"));
//
//        // load questions for survey
//        List<Question> questions = questionRepository.findBySurveyId(surveyId);
//
//        // load all question responses for survey
//        List<QuestionResponse> allQResponses = questionResponseRepository.findBySurveyResponse_Survey_Id(surveyId);
//
//        List<QuestionStatsDTO> stats = new ArrayList<>();
//
//        for (Question q : questions) {
//            QuestionStatsDTO qdto = new QuestionStatsDTO();
//            qdto.setQuestionId(q.getId());
//            qdto.setQuestionText(q.getText());
//            qdto.setQuestionType(q.getQuestionType());
//            List<QuestionResponse> responsesForQ = allQResponses.stream()
//                    .filter(r -> r.getQuestion() != null && r.getQuestion().getId().equals(q.getId()))
//                    .collect(Collectors.toList());
//
//            qdto.setTotalResponses(responsesForQ.size());
//
//            if ("RADIO".equalsIgnoreCase(q.getQuestionType())
//                    || "CHECKBOX".equalsIgnoreCase(q.getQuestionType())
//                    || "DROPDOWN".equalsIgnoreCase(q.getQuestionType())) {
//
//                Map<String, Integer> optionCounts = new LinkedHashMap<>();
//                List<String> options = q.getOptions() == null ? List.of() :
//                        Arrays.stream(q.getOptions().split(","))
//                                .map(String::trim)
//                                .filter(s -> !s.isEmpty())
//                                .collect(Collectors.toList());
//
//                // initialize counts
//                for (String opt : options) optionCounts.put(opt, 0);
//
//                // count selected
//                for (QuestionResponse qr : responsesForQ) {
//                    String ans = qr.getAnswerText();
//                    if (ans == null) continue;
//                    // checkbox might store comma-separated selections — split and count each
//                    String[] parts = ans.split(",");
//                    for (String p : parts) {
//                        String key = p.trim();
//                        if (key.isEmpty()) continue;
//                        optionCounts.put(key, optionCounts.getOrDefault(key, 0) + 1);
//                    }
//                }
//                qdto.setOptionCounts(optionCounts);
//                qdto.setTextAnswers(Collections.emptyList());
//            } else {
//                // text / textarea
//                List<String> texts = responsesForQ.stream()
//                        .map(QuestionResponse::getAnswerText)
//                        .filter(Objects::nonNull)
//                        .filter(s -> !s.isEmpty())
//                        .collect(Collectors.toList());
//                qdto.setTextAnswers(texts);
//                qdto.setOptionCounts(Collections.emptyMap());
//            }
//
//            stats.add(qdto);
//        }
//
//        return stats;
//    }
    
    @Override
    public List<QuestionStatsDTO> getQuestionStatsForSurvey(Long surveyId) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        // load all questions
        List<Question> questions = questionRepository.findBySurveyId(surveyId);

        // load all responses for this survey
        List<QuestionResponse> allQResponses =
                questionResponseRepository.findBySurveyResponse_Survey_Id(surveyId);

        List<QuestionStatsDTO> stats = new ArrayList<>();

        for (Question q : questions) {

            QuestionStatsDTO qdto = new QuestionStatsDTO();
            qdto.setQuestionId(q.getId());
            qdto.setQuestionText(q.getText());
            qdto.setQuestionType(q.getQuestionType());

            // filter responses for this question
            List<QuestionResponse> responsesForQ = allQResponses.stream()
                    .filter(r -> r.getQuestion() != null &&
                            r.getQuestion().getId().equals(q.getId()))
                    .collect(Collectors.toList());

            qdto.setTotalResponses(responsesForQ.size());

            // prepare "who answered what" list
            List<QuestionAnswerResponseDTO> answerList = responsesForQ.stream()
                    .map(qr -> {
                        QuestionAnswerResponseDTO dto = new QuestionAnswerResponseDTO();
                        dto.setQuestionId(q.getId());
                        dto.setQuestionText(q.getText());
                        dto.setAnswerText(qr.getAnswerText());

                        // fetch employee details
                        employeeRepository.findByEmployeeId(
                                qr.getSurveyResponse().getEmployeeId()
                        ).ifPresent(emp -> {
                            dto.setEmployeeId(emp.getEmployeeId());
                            dto.setEmployeeName(emp.getName());
                        });

                        return dto;
                    })
                    .collect(Collectors.toList());

            qdto.setResponses(answerList);

            // =======================================
            // OPTION BASED QUESTIONS (MCQ/CHECKBOX)
            // =======================================
            if ("RADIO".equalsIgnoreCase(q.getQuestionType())
                    || "CHECKBOX".equalsIgnoreCase(q.getQuestionType())
                    || "DROPDOWN".equalsIgnoreCase(q.getQuestionType())) {

                // initialize option counts
                Map<String, Integer> optionCounts = new LinkedHashMap<>();
                List<String> options = q.getOptions() == null ? List.of() :
                        Arrays.stream(q.getOptions().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());

                for (String opt : options) optionCounts.put(opt, 0);

                // count
                for (QuestionResponse qr : responsesForQ) {
                    if (qr.getAnswerText() == null) continue;

                    String[] selected = qr.getAnswerText().split(",");

                    for (String s : selected) {
                        String trimmed = s.trim();
                        if (!trimmed.isEmpty()) {
                            optionCounts.put(trimmed,
                                    optionCounts.getOrDefault(trimmed, 0) + 1);
                        }
                    }
                }

                qdto.setOptionCounts(optionCounts);
                qdto.setTextAnswers(Collections.emptyList());
            }

            // =======================================
            // TEXT QUESTIONS
            // =======================================
            else {
                List<String> texts = responsesForQ.stream()
                        .map(QuestionResponse::getAnswerText)
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

                qdto.setTextAnswers(texts);
                qdto.setOptionCounts(Collections.emptyMap());
            }

            stats.add(qdto);
        }

        return stats;
    }


    // ========================= NEW: return all answers for a question =========================
    @Override
    public List<QuestionAnswerResponseDTO> getAnswersForQuestion(Long surveyId, Long questionId) {
        // ensure survey exists
        surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        // ensure question exists and belongs to survey
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (q.getSurvey() == null || !q.getSurvey().getId().equals(surveyId)) {
            throw new RuntimeException("Question does not belong to specified survey");
        }

        List<QuestionResponse> answers = questionResponseRepository.findBySurveyResponse_Survey_IdAndQuestion_Id(surveyId, questionId);

        return answers.stream().map(a -> {
            QuestionAnswerResponseDTO dto = new QuestionAnswerResponseDTO();
            dto.setQuestionId(questionId);
            dto.setQuestionText(q.getText());
            dto.setAnswerText(a.getAnswerText());
            return dto;
        }).collect(Collectors.toList());
    }
}

