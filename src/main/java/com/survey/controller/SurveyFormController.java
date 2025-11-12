package com.survey.controller;

import com.survey.entity.*;
import com.survey.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class SurveyFormController {

    private final SurveyRepository surveyRepository;
    private final EmployeeRepository employeeRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final QuestionResponseRepository questionResponseRepository;

    @GetMapping("/survey/{surveyId}/form")
    public String showSurveyForm(@PathVariable Long surveyId,
                                 @RequestParam(required = false) Long employeeId,
                                 Model model) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        Employee employee = null;
        if (employeeId != null) {
            employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
        }

        model.addAttribute("survey", survey);
        model.addAttribute("employee", employee);

        return "survey-form"; 
    }

    @PostMapping("/survey/{surveyId}/submit")
    public String submitSurvey(@PathVariable Long surveyId,
                               @RequestParam Long employeeId,
                               @RequestParam Map<String, String> formData,
                               Model model) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        SurveyResponse response = SurveyResponse.builder()
                .survey(survey)
                .employee(employee)
                .submittedAt(LocalDateTime.now())
                .build();

        surveyResponseRepository.save(response);

        for (Question question : survey.getQuestions()) {
            String key = "q_" + question.getId();
            String answer = formData.get(key);

            if (answer != null && !answer.isBlank()) {
                QuestionResponse qr = QuestionResponse.builder()
                        .surveyResponse(response)
                        .question(question)
                        .answerText(answer)
                        .build();
                questionResponseRepository.save(qr);
            }
        }

        model.addAttribute("survey", survey);
        model.addAttribute("employee", employee);

        return "survey-success"; 
    }
}
