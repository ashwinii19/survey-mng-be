
package com.survey.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.survey.entity.Employee;
import com.survey.repository.EmployeeRepository;
import com.survey.repository.EmployeeBatchLogRepository;
import com.survey.entity.EmployeeBatchLog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeItemWriter implements ItemWriter<Employee> {

    private final EmployeeRepository employeeRepository;
    private final EmployeeBatchLogRepository batchLogRepository;

    @Override
    public void write(Chunk<? extends Employee> chunk) throws Exception {
        List<Employee> employees = new ArrayList<>(chunk.getItems());
        int total = employees.size();

        List<String> existingEmails = employeeRepository.findAll().stream()
                .map(Employee::getEmail)
                .collect(Collectors.toList());

        List<Employee> newEmployees = employees.stream()
                .filter(e -> !existingEmails.contains(e.getEmail()))
                .collect(Collectors.toList());

        int processed = newEmployees.size();
        int failed = total - processed;

        String status = failed == 0 ? "SUCCESS" : (processed > 0 ? "PARTIALLY_PROCESSED" : "FAILED");

        employeeRepository.saveAll(newEmployees);

        EmployeeBatchLog logEntry = new EmployeeBatchLog();
        logEntry.setTotalCount(total);
        logEntry.setProcessedCount(processed);
        logEntry.setFailedCount(failed);
        logEntry.setStatus(status);
        batchLogRepository.save(logEntry);

        log.info("Batch Summary → Total: {}, Processed: {}, Failed: {}, Status: {}", total, processed, failed, status);
    }
}
