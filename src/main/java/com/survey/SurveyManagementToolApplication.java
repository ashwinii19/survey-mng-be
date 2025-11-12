package com.survey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableBatchProcessing  
public class SurveyManagementToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(SurveyManagementToolApplication.class, args);
	}
}
