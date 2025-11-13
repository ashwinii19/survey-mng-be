package com.survey.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.survey.dto.dashboard.DashboardResponseDTO;
import com.survey.repository.SurveyRepository;
import com.survey.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final SurveyRepository surveyRepository;

    @GetMapping
    public DashboardResponseDTO getDashboard(
            @RequestParam(required = false) Long surveyId,
            @RequestParam(required = false) Long departmentId
    ) {
        return dashboardService.getDashboardData(surveyId, departmentId);
    }
    
    @GetMapping("/surveys")
    public List<String> getSurveyNames() {
        return surveyRepository.findAll()
                .stream()
                .map(s -> s.getTitle())
                .toList();
    }

}
