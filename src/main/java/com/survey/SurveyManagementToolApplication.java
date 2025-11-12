package com.survey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;

@SpringBootApplication
@EnableBatchProcessing  
public class SurveyManagementToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(SurveyManagementToolApplication.class, args);
	}
}
