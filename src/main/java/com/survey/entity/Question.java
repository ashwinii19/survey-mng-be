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

    private String text;

    @Column(name = "question_type")
    private String questionType;  // TEXT, RADIO, CHECKBOX, DROPDOWN

    @Column(columnDefinition = "TEXT")
    private String options; // Comma-separated values for choices

    @Column(nullable = false)
    private boolean required = false;  // ✅ NEW FIELD

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private Survey survey;
}
