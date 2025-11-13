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

@Controller
@RequestMapping("/survey")
@RequiredArgsConstructor
public class SurveyFormController {

    private final SurveyRepository surveyRepository;
    private final EmployeeRepository employeeRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final QuestionRepository questionRepository;
    private final QuestionResponseRepository questionResponseRepository;

    // ✅ Show the survey form
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

        return "survey_form"; // ✅ corresponds to templates/survey_form.html
    }

    @PostMapping("/{surveyId}/submit")
    public String submitSurvey(@PathVariable Long surveyId,
                               @RequestParam String employeeId,
                               @RequestParam Map<String, String> formData) {

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Save main survey response
        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setSurvey(survey);
        surveyResponse.setEmployeeId(employeeId);
        surveyResponse.setSubmittedAt(LocalDateTime.now());
        surveyResponseRepository.save(surveyResponse);

        // Save each question’s response
        List<Question> questions = questionRepository.findBySurveyId(surveyId);
        for (Question q : questions) {
            String key = "q_" + q.getId();
            String answer = formData.get(key);

            if (answer != null && !answer.isBlank()) {
                QuestionResponse qr = new QuestionResponse();
                qr.setSurveyResponse(surveyResponse);
                qr.setQuestion(q);
                qr.setAnswerText(answer);
                qr.setEmployeeId(employeeId);
                questionResponseRepository.save(qr);
            }
        }

        return "survey_success";
    }

}
