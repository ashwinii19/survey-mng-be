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
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/survey")
public class SurveyFormController {

    private final SurveyRepository surveyRepository;
    private final EmployeeRepository employeeRepository;
    private final SurveyResponseRepository responseRepository;
    private final QuestionRepository questionRepository;
    private final QuestionResponseRepository questionResponseRepository;

    // ✅ Show the survey form with all questions
    @GetMapping("/{id}/form")
    public String showSurveyForm(@PathVariable Long id,
                                 @RequestParam(required = false) Long employeeId,
                                 Model model) {

        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        model.addAttribute("survey", survey);
        model.addAttribute("employeeId", employeeId);

        return "survey-form"; // Thymeleaf template
    }

    // ✅ Handle survey form submission
    @PostMapping("/{id}/submit")
    public String handleSurveySubmission(@PathVariable Long id,
                                         @RequestParam Long employeeId,
                                         @RequestParam Map<String, String> answers,
                                         Model model) {

        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Remove hidden fields
        answers.remove("employeeId");

        // ✅ Create a SurveyResponse (main record)
        SurveyResponse surveyResponse = SurveyResponse.builder()
                .survey(survey)
                .employee(emp)
                .submittedAt(LocalDateTime.now())
                .build();
        SurveyResponse savedResponse = responseRepository.save(surveyResponse);

        // ✅ Save each question's response
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            String key = entry.getKey(); // like q_5
            String answerText = entry.getValue();

            if (key.startsWith("q_")) {
                Long questionId = Long.parseLong(key.substring(2)); // extract question ID
                Optional<Question> questionOpt = questionRepository.findById(questionId);
                if (questionOpt.isPresent()) {
                    Question question = questionOpt.get();
                    QuestionResponse qr = QuestionResponse.builder()
                            .surveyResponse(savedResponse)
                            .question(question)
                            .answerText(answerText)
                            .build();
                    questionResponseRepository.save(qr);
                }
            }
        }

        model.addAttribute("message", " Your responses have been submitted successfully!");
        return "survey-success";
    }
}
