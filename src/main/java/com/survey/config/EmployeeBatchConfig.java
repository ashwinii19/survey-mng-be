package com.survey.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;  // ⬅️ ADD THIS IMPORT
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.survey.dto.request.EmployeeRequestDTO;
import com.survey.entity.Employee;
import com.survey.batch.EmployeeFieldSetMapper;

@Configuration
public class EmployeeBatchConfig {

    // Step 1: Reader
    @Bean
    @StepScope
    public FlatFileItemReader<EmployeeRequestDTO> employeeItemReader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        FlatFileItemReader<EmployeeRequestDTO> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(filePath));
        reader.setName("employeeCsvReader");
        reader.setLinesToSkip(1);
        reader.setLineMapper(employeeLineMapper());

        reader.setSkippedLinesCallback(line -> {
            if (line.trim().isEmpty()) {
                System.out.println("Skipping empty line...");
            }
        });

        return reader;
    }

    // Step 2: Line Mapper
    @Bean
    public LineMapper<EmployeeRequestDTO> employeeLineMapper() {
        DefaultLineMapper<EmployeeRequestDTO> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setStrict(false);
        tokenizer.setNames("employeeId", "name", "email", "position", "status", "joinDate", "departmentId");

        FieldSetMapper<EmployeeRequestDTO> fieldSetMapper = new EmployeeFieldSetMapper();

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    // Step 3: Step
    @Bean
    public Step employeeImportStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   ItemReader<EmployeeRequestDTO> reader,
                                   ItemProcessor<EmployeeRequestDTO, Employee> processor,
                                   ItemWriter<Employee> writer) {

        return new StepBuilder("employeeImportStep", jobRepository)
                .<EmployeeRequestDTO, Employee>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // Step 4: Job - FIXED
    @Bean
    public Job importEmployeeJob(JobRepository jobRepository, Step employeeImportStep) {
        return new JobBuilder("importEmployeeJob", jobRepository)
                .incrementer(new RunIdIncrementer())  // ⬅️ THIS CREATES JOB INSTANCES
                .start(employeeImportStep)
                .build();
    }
}