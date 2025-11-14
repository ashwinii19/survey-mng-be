package com.survey.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_response_id")
    private SurveyResponse surveyResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;
}
