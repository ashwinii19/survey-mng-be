package com.survey.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // QUESTION TEXT
    @Column(nullable = false)
    private String text;

    // TEXT, TEXTAREA, RADIO, CHECKBOX, DROPDOWN
    @Column(name = "question_type", nullable = false)
    private String questionType;

    // Comma-separated options
    @Column(columnDefinition = "TEXT")
    private String options;

    private boolean required;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private Survey survey;

}
