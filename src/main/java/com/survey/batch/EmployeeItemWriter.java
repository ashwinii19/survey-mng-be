package com.survey.batch;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.survey.entity.Employee;
import com.survey.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmployeeItemWriter implements ItemWriter<Employee> {

    private final EmployeeRepository employeeRepository;

    @Override
    public void write(Chunk<? extends Employee> chunk) throws Exception {
        List<Employee> employees = new ArrayList<>(chunk.getItems());
        employeeRepository.saveAll(employees);
    }
}
