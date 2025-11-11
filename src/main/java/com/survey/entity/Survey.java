package com.survey.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "surveys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Survey {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;
	@Column(length = 2000)
	private String description;

	private boolean published;
	private LocalDateTime createdAt;
	private LocalDateTime publishedAt;

	private String formLink;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_department_id", nullable = true)
	private Department targetDepartment;

	@OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Question> questions = new ArrayList<>();
}
