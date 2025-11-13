package com.survey.controller;

import com.survey.entity.*;
import com.survey.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/survey")
@RequiredArgsConstructor
public class SurveyFormController {

    private final SurveyRepository surveyRepository;
    private final EmployeeRepository employeeRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final QuestionRepository questionRepository;
    private final QuestionResponseRepository questionResponseRepository;

    @GetMapping("/{surveyId}/form")
    public String showSurveyForm(@PathVariable Long surveyId,
                                 @RequestParam String employeeId,
                                 Model model) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        model.addAttribute("survey", survey);
        model.addAttribute("employee", employee);
        return "survey_form";
    }

    @PostMapping("/{surveyId}/submit")
    public String submitSurvey(@PathVariable Long surveyId,
                               @RequestParam String employeeId,
                               @RequestParam Map<String, String> formData,
                               Model model) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Save main response
        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);
        response.setEmployeeId(employeeId);
        response.setSubmittedAt(LocalDateTime.now());
        surveyResponseRepository.save(response);

        // Save questions
        List<Question> questions = questionRepository.findBySurveyId(surveyId);

        for (Question q : questions) {

            String key = "q_" + q.getId();
            String answer = formData.get(key);

            // CHECKBOX MULTIPLE VALUES
            if (q.getQuestionType().equalsIgnoreCase("CHECKBOX")) {
                List<String> selected = formData.entrySet().stream()
                        .filter(e -> e.getKey().startsWith(key))
                        .map(Map.Entry::getValue)
                        .toList();
                answer = String.join(", ", selected);
            }

            if (answer != null && !answer.isBlank()) {
                QuestionResponse qr = new QuestionResponse();
                qr.setSurveyResponse(response);
                qr.setQuestion(q);
                qr.setEmployeeId(employeeId);
                qr.setAnswerText(answer.trim());
                questionResponseRepository.save(qr);

                System.out.println("Saved: " + q.getText() + " -> " + answer);
            }
        }

        return "survey_success";
    }

}
