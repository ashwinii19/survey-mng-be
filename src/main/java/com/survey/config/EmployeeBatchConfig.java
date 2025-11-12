package com.survey.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.survey.dto.request.EmployeeRequestDTO;
import com.survey.entity.Employee;
import com.survey.batch.EmployeeFieldSetMapper;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class EmployeeBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    // Step 1: Reader — Reads from CSV file
    @Bean
    @StepScope
    public FlatFileItemReader<EmployeeRequestDTO> employeeItemReader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        FlatFileItemReader<EmployeeRequestDTO> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(filePath));
        reader.setName("employeeCsvReader");
        reader.setLinesToSkip(1); // Skip header row
        reader.setLineMapper(employeeLineMapper());

        // Optional: Skip empty lines
        reader.setSkippedLinesCallback(line -> {
            if (line.trim().isEmpty()) {
                System.out.println("Skipping empty line...");
            }
        });

        return reader;
    }

    // Step 2: Line Mapper — Maps CSV columns to DTO fields
    @Bean
    public LineMapper<EmployeeRequestDTO> employeeLineMapper() {
        DefaultLineMapper<EmployeeRequestDTO> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(","); // use "\t" if tab-separated
        tokenizer.setStrict(false);

        // 🧩 Must match your CSV header order
        tokenizer.setNames("employeeId", "name", "email", "position", "status", "joinDate", "departmentId");

        FieldSetMapper<EmployeeRequestDTO> fieldSetMapper = new EmployeeFieldSetMapper();

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    // Step 3: Step definition
    @Bean
    public Step employeeImportStep(ItemReader<EmployeeRequestDTO> reader,
                                   ItemProcessor<EmployeeRequestDTO, Employee> processor,
                                   ItemWriter<Employee> writer) {

        return new StepBuilder("employeeImportStep", jobRepository)
                .<EmployeeRequestDTO, Employee>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // Step 4: Job definition
    @Bean
    public Job importEmployeeJob(Step employeeImportStep) {
        return new JobBuilder("importEmployeeJob", jobRepository)
                .start(employeeImportStep)
                .build();
    }
}
