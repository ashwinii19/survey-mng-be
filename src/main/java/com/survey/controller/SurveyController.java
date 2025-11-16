package com.survey.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.survey.dto.request.SurveyRequestDTO;
import com.survey.dto.response.SurveyResponseDTO;
import com.survey.service.SurveyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping
    public ResponseEntity<SurveyResponseDTO> create(@Valid @RequestBody SurveyRequestDTO dto) {
        return ResponseEntity.ok(surveyService.createSurvey(dto));
    }

    @GetMapping
    public ResponseEntity<List<SurveyResponseDTO>> getAll() {
        return ResponseEntity.ok(surveyService.getAllSurveys());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SurveyResponseDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.getSurvey(id));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<SurveyResponseDTO> publish(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.publishSurvey(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        surveyService.deleteSurvey(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SurveyResponseDTO> updateSurvey(
            @PathVariable Long id,
            @Valid @RequestBody SurveyRequestDTO dto) {
        return ResponseEntity.ok(surveyService.updateSurvey(id, dto));
    }

}
