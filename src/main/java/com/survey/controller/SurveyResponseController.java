package com.survey.controller;

import com.survey.dto.response.SurveySubmissionResponseDTO;
import com.survey.service.SurveyResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/survey-responses")
@RequiredArgsConstructor
public class SurveyResponseController {

    private final SurveyResponseService surveyResponseService;

    @GetMapping
    public ResponseEntity<List<SurveySubmissionResponseDTO>> getAll() {
        return ResponseEntity.ok(surveyResponseService.getAllSurveyResponsesSummary());
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveySubmissionResponseDTO> getBySurvey(@PathVariable Long surveyId) {
        return ResponseEntity.ok(surveyResponseService.getSurveyResponseSummary(surveyId));
    }

    @GetMapping("/{surveyId}/department/{departmentId}")
    public ResponseEntity<SurveySubmissionResponseDTO> getBySurveyAndDepartment(
            @PathVariable Long surveyId,
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(surveyResponseService.getSurveyResponseSummaryByDept(surveyId, departmentId));
    }

    @GetMapping("/filter")
    public ResponseEntity<SurveySubmissionResponseDTO> getFilteredResponses(
            @RequestParam Long surveyId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        return ResponseEntity.ok(
                surveyResponseService.getFilteredSurveyResponses(
                        surveyId, departmentId, employeeName, fromDate, toDate
                )
        );
    }
}
