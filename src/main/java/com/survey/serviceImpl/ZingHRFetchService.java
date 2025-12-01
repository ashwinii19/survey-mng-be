//package com.survey.serviceImpl;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.thymeleaf.context.Context;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.survey.dto.external.ZingEmployeeDTO;
//import com.survey.dto.external.ZingEmployeeResponse;
//import com.survey.entity.Employee;
//import com.survey.mapper.ZingEmployeeMapper;
//import com.survey.repository.EmployeeRepository;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class ZingHRFetchService {
//
//    private final EmployeeRepository employeeRepository;
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
//
//    public void fetchEmployeesAndSend() {
//        try {
//            HttpHeaders tokenHeaders = new HttpHeaders();
//            tokenHeaders.setBasicAuth(USERNAME, PASSWORD);
//
//            ResponseEntity<String> tokenResp =
//                    restTemplate.exchange(TOKEN_URL, HttpMethod.GET,
//                            new HttpEntity<>(tokenHeaders),
//                            String.class);
//
//            String jwtToken = tokenResp.getBody();
//
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(jwtToken);
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            String json = restTemplate.exchange(
//                    EMP_URL, HttpMethod.POST, new HttpEntity<>(headers),
//                    String.class).getBody();
//
//
//            ZingEmployeeResponse response =
//                    mapper.readValue(json, ZingEmployeeResponse.class);
//
//            List<ZingEmployeeDTO> employees = response.getData().getEmployees();
//
//
//            for (ZingEmployeeDTO dto : employees) {
//
//                if (dto.getEmail() == null || dto.getEmail().isBlank()) {
//                    continue;
//                }
//
//                Optional<Employee> existing = employeeRepository.findByEmail(dto.getEmail());
//                if (existing.isPresent()) {
//                    continue;
//                }
//
//                Employee emp = ZingEmployeeMapper.map(dto);
//
////                employeeRepository.save(emp);
//                
//                System.out.println("TEST — Employee mapped: " + emp.getEmail());
//
//                if (isEligible(dto.getDateOfJoining())) {
//
//                    Context context = new Context();
//                    context.setVariable("employeeName", emp.getName());
//                    context.setVariable("employeeId", emp.getEmployeeId());
//                    context.setVariable("employeeEmail", emp.getEmail());
//                    context.setVariable("joinDate", emp.getJoinDate());
//                    context.setVariable("currentDate", LocalDate.now());
//
//                    emailService.sendEmailWithTemplate(
//                            emp.getEmail(),
//                            "Welcome to Aurionpro - " + emp.getName(),
//                            "onboarding-welcome",
//                            context
//                    );
//
//                    log.info("Welcome email sent to: {}", emp.getEmail());
//                }
//            }
//
//        } catch (Exception e) {
//            log.error("Error fetching ZingHR employees: {}", e.getMessage());
//        }
//    }
//
//
//    private boolean isEligible(String doj) {
//        try {
//            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
//
//            LocalDate joinDate = LocalDate.parse(doj, fmt);
//
//            long days = ChronoUnit.DAYS.between(joinDate, LocalDate.now());
//
//            return days >= 0 && days <= 15;
//
//        } catch (Exception e) {
//            return false;
//        }
//    }
//}






package com.survey.serviceImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.survey.dto.external.ZingEmployeeDTO;
import com.survey.dto.external.ZingEmployeeRequest;
import com.survey.dto.external.ZingEmployeeResponse;
import com.survey.entity.Employee;
import com.survey.mapper.ZingEmployeeMapper;
import com.survey.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ZingHRFetchService {

    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final RestTemplate restTemplate;
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


    public void fetchEmployeesAndSend() {

        log.info("ZingHR Fetch STARTED | TEST MODE = {}", TEST_MODE);

        String token;
        try {
            HttpHeaders h = new HttpHeaders();
            h.setBasicAuth(USERNAME, PASSWORD);

            ResponseEntity<String> res = restTemplate.exchange(
                    TOKEN_URL,
                    HttpMethod.GET,
                    new HttpEntity<>(h),
                    String.class
            );

            JsonNode json = mapper.readTree(res.getBody());
            token = json.get("data").asText();

            log.info("Token fetched");

        } catch (Exception ex) {
            log.error("Failed to fetch token: {}", ex.getMessage());
            return;
        }


        String empJson;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ZingEmployeeRequest reqBody = new ZingEmployeeRequest();
            reqBody.setPageNumber(1);
            reqBody.setPageSize(500);

            HttpEntity<ZingEmployeeRequest> req =
                    new HttpEntity<>(reqBody, headers);

            ResponseEntity<String> empRes = restTemplate.exchange(
                    EMP_URL,
                    HttpMethod.POST,
                    req,
                    String.class
            );

            empJson = empRes.getBody();
            log.info("Employee list fetched");

        } catch (Exception ex) {
            log.error("Failed fetching employees: {}", ex.getMessage());
            return;
        }


        List<ZingEmployeeDTO> list;
        try {
            ZingEmployeeResponse parsed =
                    mapper.readValue(empJson, ZingEmployeeResponse.class);

            list = parsed.getData().getEmployees();

            log.info("Total employees received = {}", list.size());

        } catch (Exception ex) {
            log.error("Parsing error: {}", ex.getMessage());
            return;
        }


        for (ZingEmployeeDTO dto : list) {
            try {
                if (dto.getEmail() == null || dto.getEmail().isBlank())
                    continue;

                Optional<Employee> existing =
                        employeeRepository.findByEmail(dto.getEmail());

                if (existing.isPresent())
                    continue;

                Employee emp = ZingEmployeeMapper.map(dto);


                if (TEST_MODE) {
                    log.warn("TEST MODE → NOT saving {}", emp.getEmail());
                    log.warn("TEST MODE → NOT sending email {}", emp.getEmail());
                    continue;
                }


                employeeRepository.save(emp);
                log.info("Saved: {}", emp.getEmail());


                if (isEligible(emp.getJoinDate())) {

                    Context ctx = new Context();
                    ctx.setVariable("employeeName", emp.getName());
                    ctx.setVariable("employeeId", emp.getEmployeeId());
                    ctx.setVariable("employeeEmail", emp.getEmail());
                    ctx.setVariable("joinDate", emp.getJoinDate());
                    ctx.setVariable("currentDate", LocalDate.now());

                    emailService.sendEmailWithTemplate(
                            emp.getEmail(),
                            "Welcome to Aurionpro - " + emp.getName(),
                            "onboarding-welcome",
                            ctx
                    );

                    log.info("Email sent: {}", emp.getEmail());
                }

            } catch (Exception ex) {
                log.error("Error processing {} - {}", dto.getEmail(), ex.getMessage());
            }
        }
    }


    private boolean isEligible(String doj) {
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
