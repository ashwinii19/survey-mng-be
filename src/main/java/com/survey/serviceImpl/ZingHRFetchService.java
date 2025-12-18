//package com.survey.serviceImpl;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.survey.dto.external.ZingEmployeeDTO;
//import com.survey.dto.external.ZingEmployeeRequest;
//import com.survey.dto.external.ZingEmployeeResponse;
//
//import com.survey.entity.Department;
//import com.survey.entity.Employee;
//
//import com.survey.mapper.ZingEmployeeMapper;
//import com.survey.repository.DepartmentRepository;
//import com.survey.repository.EmployeeRepository;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;
//import org.thymeleaf.context.Context;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class ZingHRFetchService {
//
//    private final EmployeeRepository employeeRepository;
//    private final DepartmentRepository departmentRepository;
//    private final EmailService emailService;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    @Value("${zinghr.token.url}")
//    private String TOKEN_URL;
//
//    @Value("${zinghr.employee.url}")
//    private String EMP_URL;
//
//    @Value("${zinghr.username}")
//    private String USERNAME;
//
//    @Value("${zinghr.password}")
//    private String PASSWORD;
//
//    @Value("${zinghr.test-mode:true}")
//    private boolean TEST_MODE;
//
//
//    // -----------------------------------------------------------------
//    // MAIN ENTRY → Fetch ALL employees + save + email
//    // -----------------------------------------------------------------
//    public void fetchEmployeesAndSend() {
//
//        log.info("📌 ZingHR SYNC STARTED | TEST_MODE = {}", TEST_MODE);
//
//        String token = fetchToken();
//        if (token == null) return;
//
//        List<ZingEmployeeDTO> all = fetchAllEmployees(token);
//
//        log.info("🟦 TOTAL EMPLOYEES RECEIVED = {}", all.size());
//
//        all.forEach(this::processDto);
//
//        log.info("🎉 SYNC COMPLETED");
//    }
//
//
//    // -----------------------------------------------------------------
//    // AUTH TOKEN
//    // -----------------------------------------------------------------
//    private String fetchToken() {
//        try {
//            HttpHeaders h = new HttpHeaders();
//            h.setBasicAuth(USERNAME, PASSWORD);
//
//            ResponseEntity<String> res = restTemplate.exchange(
//                    TOKEN_URL, HttpMethod.GET, new HttpEntity<>(h), String.class);
//
//            JsonNode json = mapper.readTree(res.getBody());
//            return json.get("data").asText();
//
//        } catch (Exception ex) {
//            log.error("❌ Failed to fetch token: {}", ex.getMessage());
//            return null;
//        }
//    }
//
//
//    // -----------------------------------------------------------------
//    // FETCH ALL PAGES
//    // -----------------------------------------------------------------
//    private List<ZingEmployeeDTO> fetchAllEmployees(String token) {
//
//        List<ZingEmployeeDTO> all = new ArrayList<>();
//        int page = 1;
//        int size = 500;
//
//        while (true) {
//            List<ZingEmployeeDTO> batch = fetchPage(token, page, size);
//
//            if (batch == null || batch.isEmpty())
//                break;
//
//            all.addAll(batch);
//
//            if (batch.size() < size)
//                break;
//
//            page++;
//        }
//
//        return all;
//    }
//
//
//    // -----------------------------------------------------------------
//    // FETCH SINGLE PAGE (NO DATE FILTER)
//    // -----------------------------------------------------------------
//    private List<ZingEmployeeDTO> fetchPage(String token, int page, int size) {
//
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(token);
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            // ZingHR requires DOJ filter → use very old date
//            ZingEmployeeRequest reqBody = new ZingEmployeeRequest();
//            reqBody.setFromDate("01-01-1997");
//            reqBody.setToDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
//            reqBody.setPageNumber(page);
//            reqBody.setPageSize(size);
//
//            HttpEntity<ZingEmployeeRequest> req = new HttpEntity<>(reqBody, headers);
//
//            ResponseEntity<String> res = restTemplate.exchange(
//                    EMP_URL, HttpMethod.POST, req, String.class);
//
//            ZingEmployeeResponse parsed = mapper.readValue(res.getBody(), ZingEmployeeResponse.class);
//            if (parsed.getData() == null) return Collections.emptyList();
//
//            return parsed.getData().getEmployees();
//
//        } catch (Exception ex) {
//            log.error("❌ Page fetch failed {} → {}", page, ex.getMessage());
//            return Collections.emptyList();
//        }
//    }
//
//
//    // -----------------------------------------------------------------
//    // EMPLOYEE UPSERT + EMAIL
//    // -----------------------------------------------------------------
//    @Transactional
//    protected void processDto(ZingEmployeeDTO dto) {
//
//        if (dto.getEmployeeCode() == null)
//            return;
//
//        Optional<Employee> existing = employeeRepository.findByEmployeeId(dto.getEmployeeCode());
//
//        boolean isNew = false;
//        Employee emp;
//
//        if (existing.isPresent()) {
//            emp = existing.get();
//            ZingEmployeeMapper.mapToExisting(emp, dto);
//        } else {
//            emp = ZingEmployeeMapper.mapToNew(dto);
//            isNew = true;
//        }
//
//        // Resolve department AFTER mapping
//        resolveDepartment(emp, dto);
//
//        if (!TEST_MODE) {
//            employeeRepository.save(emp);
//            log.info("✔ {} {}", isNew ? "NEW" : "UPDATED", emp.getEmployeeId());
//        }
//
//        // Send email only for new employees inside 15 days
//        if (!TEST_MODE && isNew && isEligible(emp.getJoinDate())) {
//            sendWelcomeEmail(emp);
//        }
//    }
//
//
//    // -----------------------------------------------------------------
//    // DEPARTMENT LOGIC
//    // -----------------------------------------------------------------
//    @Transactional
//    protected void resolveDepartment(Employee emp, ZingEmployeeDTO dto) {
//
//        // extract designation/department name from attributes
//        String deptName = ZingEmployeeMapper.extractDeptName(dto);
//
//        if (deptName == null || deptName.isBlank()) {
//            emp.setDepartment(null);
//            return;
//        }
//
//        // check existing department in DB
//        Optional<Department> optionalDept = departmentRepository.findByNameIgnoreCase(deptName);
//
//        // if not present → create new
//        Department dept = optionalDept.orElseGet(() -> {
//            Department d = new Department();
//            d.setName(deptName);
//            return d;
//        });
//
//        // if newly created (id = null) → save
//        if (dept.getId() == null) {
//            dept = departmentRepository.save(dept);
//        }
//
//        // finally assign to employee
//        emp.setDepartment(dept);
//    }
//
//
//
//    // -----------------------------------------------------------------
//    // EMAIL SENDING
//    // -----------------------------------------------------------------
//    private void sendWelcomeEmail(Employee emp) {
//        try {
//            Context ctx = new Context();
//            ctx.setVariable("employeeName", emp.getName());
//            ctx.setVariable("employeeId", emp.getEmployeeId());
//            ctx.setVariable("joinDate", emp.getJoinDate());
//
//            emailService.sendEmailWithTemplate(
//                    emp.getEmail(),
//                    "Welcome to Aurionpro - " + emp.getName(),
//                    "onboarding-welcome",
//                    ctx
//            );
//
//            log.info("📧 Email sent → {}", emp.getEmail());
//
//        } catch (Exception ex) {
//            log.error("❌ Email failed → {} : {}", emp.getEmail(), ex.getMessage());
//        }
//    }
//
//
//    // -----------------------------------------------------------------
//    // EMAIL ELIGIBILITY (DOJ within 15 days)
//    // -----------------------------------------------------------------
//    private boolean isEligible(String doj) {
//
//        if (doj == null || doj.isBlank())
//            return false;
//
//        try {
//            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
//            LocalDate joinDate = LocalDate.parse(doj.trim(), fmt);
//
//            long days = ChronoUnit.DAYS.between(joinDate, LocalDate.now());
//            return days >= 0 && days <= 15;
//
//        } catch (Exception ex) {
//            return false;
//        }
//    }
//}
//






















package com.survey.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.dto.external.ZingEmployeeDTO;
import com.survey.dto.external.ZingEmployeeRequest;
import com.survey.dto.external.ZingEmployeeResponse;
import com.survey.entity.Department;
import com.survey.entity.Employee;
import com.survey.mapper.ZingEmployeeMapper;
import com.survey.repository.DepartmentRepository;
import com.survey.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;

import jakarta.annotation.PostConstruct;  // <-- added for logging

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ZingHRFetchService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmailService emailService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${zinghr.token.url}")
    private String TOKEN_URL;

    @Value("${zinghr.employee.url}")
    private String EMP_URL;

    @Value("${zinghr.username}")
    private String USERNAME;

    @Value("${zinghr.password}")
    private String PASSWORD;

    @Value("${zinghr.test-mode:true}")
    private boolean TEST_MODE;

    // ---------------------------------------------
    // Log properties at startup
    // ---------------------------------------------
//    @PostConstruct
//    public void logProperties() {
//        log.info("📌 ZingHR Properties at Startup:");
//        log.info("TOKEN_URL = {}", TOKEN_URL);
//        log.info("EMP_URL = {}", EMP_URL);
//        log.info("USERNAME = {}", USERNAME);
//        log.info("PASSWORD = {}", PASSWORD != null ? "*****" : null); // hide password
//        log.info("TEST_MODE = {}", TEST_MODE);
//    }

    // -----------------------------------------------------------------
    // MAIN ENTRY → Fetch ALL employees + save + email
    // -----------------------------------------------------------------
    
    
   
    
    
    public void fetchEmployeesAndSend() {

        log.info("📌 ZingHR SYNC STARTED | TEST_MODE = {}", TEST_MODE);

        String token = fetchToken();
        if (token == null) return;

        List<ZingEmployeeDTO> all = fetchAllEmployees(token);

        log.info("🟦 TOTAL EMPLOYEES RECEIVED = {}", all.size());

        all.forEach(this::processDto);

        log.info("🎉 SYNC COMPLETED");
    }

    // -----------------------------------------------------------------
    // AUTH TOKEN
    // -----------------------------------------------------------------
    private String fetchToken() {
        try {
            HttpHeaders h = new HttpHeaders();
            h.setBasicAuth(USERNAME, PASSWORD);

            ResponseEntity<String> res = restTemplate.exchange(
                    TOKEN_URL, HttpMethod.GET, new HttpEntity<>(h), String.class);

            JsonNode json = mapper.readTree(res.getBody());
            return json.get("data").asText();

        } catch (Exception ex) {
            log.error("❌ Failed to fetch token: {}", ex.getMessage());
            return null;
        }
    }

    // -----------------------------------------------------------------
    // FETCH ALL PAGES
    // -----------------------------------------------------------------
    private List<ZingEmployeeDTO> fetchAllEmployees(String token) {

        List<ZingEmployeeDTO> all = new ArrayList<>();
        int page = 1;
        int size = 500;

        while (true) {
            List<ZingEmployeeDTO> batch = fetchPage(token, page, size);

            if (batch == null || batch.isEmpty())
                break;

            all.addAll(batch);

            if (batch.size() < size)
                break;

            page++;
        }

        return all;
    }

    // -----------------------------------------------------------------
    // FETCH SINGLE PAGE (NO DATE FILTER)
    // -----------------------------------------------------------------
    private List<ZingEmployeeDTO> fetchPage(String token, int page, int size) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // ZingHR requires DOJ filter → use very old date
            ZingEmployeeRequest reqBody = new ZingEmployeeRequest();
            reqBody.setFromDate("01-01-1997");
            reqBody.setToDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            reqBody.setPageNumber(page);
            reqBody.setPageSize(size);

            HttpEntity<ZingEmployeeRequest> req = new HttpEntity<>(reqBody, headers);

            ResponseEntity<String> res = restTemplate.exchange(
                    EMP_URL, HttpMethod.POST, req, String.class);

            ZingEmployeeResponse parsed = mapper.readValue(res.getBody(), ZingEmployeeResponse.class);
            if (parsed.getData() == null) return Collections.emptyList();

            return parsed.getData().getEmployees();

        } catch (Exception ex) {
            log.error("❌ Page fetch failed {} → {}", page, ex.getMessage());
            return Collections.emptyList();
        }
    }

    // -----------------------------------------------------------------
    // EMPLOYEE UPSERT + EMAIL
    // -----------------------------------------------------------------
    @Transactional
    protected void processDto(ZingEmployeeDTO dto) {

        if (dto.getEmployeeCode() == null)
            return;

        Optional<Employee> existing = employeeRepository.findByEmployeeId(dto.getEmployeeCode());

        boolean isNew = false;
        Employee emp;

        if (existing.isPresent()) {
            emp = existing.get();
            ZingEmployeeMapper.mapToExisting(emp, dto);
        } else {
            emp = ZingEmployeeMapper.mapToNew(dto);
            isNew = true;
        }

        // Resolve department AFTER mapping
        resolveDepartment(emp, dto);

        if (!TEST_MODE) {
            employeeRepository.save(emp);
            log.info("✔ {} {}", isNew ? "NEW" : "UPDATED", emp.getEmployeeId());
        }

        // Send email only for new employees inside 15 days
        if (!TEST_MODE && isNew && isEligible(emp.getJoinDate())) {
            sendWelcomeEmail(emp);
        }
    }

    // -----------------------------------------------------------------
    // DEPARTMENT LOGIC
    // -----------------------------------------------------------------
    @Transactional
    protected void resolveDepartment(Employee emp, ZingEmployeeDTO dto) {

        // extract designation/department name from attributes
        String deptName = ZingEmployeeMapper.extractDeptName(dto);

        if (deptName == null || deptName.isBlank()) {
            emp.setDepartment(null);
            return;
        }

        // check existing department in DB
        Optional<Department> optionalDept = departmentRepository.findByNameIgnoreCase(deptName);

        // if not present → create new
        Department dept = optionalDept.orElseGet(() -> {
            Department d = new Department();
            d.setName(deptName);
            return d;
        });

        // if newly created (id = null) → save
        if (dept.getId() == null) {
            dept = departmentRepository.save(dept);
        }

        // finally assign to employee
        emp.setDepartment(dept);
    }

    // -----------------------------------------------------------------
    // EMAIL SENDING
    // -----------------------------------------------------------------
    private void sendWelcomeEmail(Employee emp) {
        try {
            Context ctx = new Context();
            ctx.setVariable("employeeName", emp.getName());
            ctx.setVariable("employeeId", emp.getEmployeeId());
            ctx.setVariable("joinDate", emp.getJoinDate());

            emailService.sendEmailWithTemplate(
                    emp.getEmail(),
                    "Welcome to Aurionpro - " + emp.getName(),
                    "onboarding-welcome",
                    ctx
            );

            log.info("📧 Email sent → {}", emp.getEmail());

        } catch (Exception ex) {
            log.error("❌ Email failed → {} : {}", emp.getEmail(), ex.getMessage());
        }
    }

    // -----------------------------------------------------------------
    // EMAIL ELIGIBILITY (DOJ within 15 days)
    // -----------------------------------------------------------------
    private boolean isEligible(String doj) {

        if (doj == null || doj.isBlank())
            return false;

        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
            LocalDate joinDate = LocalDate.parse(doj.trim(), fmt);

            long days = ChronoUnit.DAYS.between(joinDate, LocalDate.now());
            return days >= 0 && days <= 15;

        } catch (Exception ex) {
            return false;
        }
    }
}
