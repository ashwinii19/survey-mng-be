//package com.survey.service;
//
//import com.survey.b.entity.BatchJobExecution;
//import com.survey.b.entity.BatchJobInstance;
//import com.survey.repository.BatchJobInstanceRepository;
//import com.survey.repository.BatchJobExecutionRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//@Service
//public class BatchJobService {
//
//    @Autowired
//    private BatchJobInstanceRepository batchJobInstanceRepository;
//    
//    @Autowired
//    private BatchJobExecutionRepository batchJobExecutionRepository;
//
//    @Transactional
//    public Long createBatchJobWithExecution() {
//        try {
//            // 1. Create and save the parent first
//            BatchJobInstance jobInstance = new BatchJobInstance();
//            jobInstance.setJobName("Employee Import");
//            jobInstance.setJobKey("EMP_IMPORT_" + System.currentTimeMillis());
//            jobInstance.setVersion(1L);
//            
//            BatchJobInstance savedInstance = batchJobInstanceRepository.save(jobInstance);
//            
//            // 2. Create and save child execution separately
//            BatchJobExecution execution = new BatchJobExecution();
//            execution.setJobInstance(savedInstance);
//            execution.setStartTime(LocalDateTime.now());
//            execution.setStatus("STARTED");
//            execution.setExitCode("UNKNOWN");
//            execution.setExitMessage("");
//            execution.setVersion(1L);
//            execution.setCreateTime(LocalDateTime.now());
//            execution.setLastUpdated(LocalDateTime.now());
//
//            BatchJobExecution savedExecution = batchJobExecutionRepository.save(execution);
//            
//            System.out.println("Batch job instance and execution saved successfully!");
//            System.out.println("Job Instance ID: " + savedInstance.getJobInstanceId());
//            System.out.println("Job Execution ID: " + savedExecution.getJobExecutionId());
//            
//            return savedExecution.getJobExecutionId();
//            
//        } catch (Exception e) {
//            System.err.println("Error saving batch job: " + e.getMessage());
//            e.printStackTrace();
//            throw e;
//        }
//    }
//}