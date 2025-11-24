//
//
//
//
//
//
//package com.survey.serviceImpl;
//
//import com.survey.entity.OnboardingEmailLog;
//import com.survey.entity.Employee;
//import com.survey.entity.EmployeeBatchLog;
//import com.survey.repository.OnboardingEmailLogRepository;
//import com.survey.repository.EmployeeBatchLogRepository;
//import com.survey.repository.EmployeeRepository;
//import com.survey.service.OnboardingEmailService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import java.time.LocalDateTime;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class OnboardingEmailServiceImpl implements OnboardingEmailService {
//
//    private final EmployeeRepository employeeRepository;
//    private final EmployeeBatchLogRepository employeeBatchLogRepository;
//    private final OnboardingEmailLogRepository onboardingEmailLogRepository;
//    private final EmailService emailService;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    
//    // Multiple date formats to handle different input sources
//    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
//        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
//        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
//        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
//        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
//        DateTimeFormatter.ofPattern("MM-dd-yyyy"),
//        DateTimeFormatter.ofPattern("dd/MM/yyyy")
//    );
//    
//    private static final int MAX_DAYS_SINCE_JOINING = 15;
//
//    @Async
//    @Override
//    public void sendWelcomeEmailsForBatch(Long employeeBatchLogId) {
//        log.info("Starting onboarding welcome emails for batch: {}", employeeBatchLogId);
//        
//        // ✅ CHECK if emails have already been sent for this batch using OnboardingEmailLogRepository
//        OnboardingEmailLog existingLog = onboardingEmailLogRepository.findByEmployeeBatchLogId(employeeBatchLogId);
//        if (existingLog != null && "COMPLETED".equals(existingLog.getStatus())) {
//            log.info("Emails have already been sent for batch {}. Skipping...", employeeBatchLogId);
//            return;
//        }
//        
//        // Create email log entry
//        OnboardingEmailLog onboardingEmailLog = new OnboardingEmailLog();
//        onboardingEmailLog.setEmployeeBatchLogId(employeeBatchLogId);
//        onboardingEmailLog.setStatus("PROCESSING");
//        onboardingEmailLog = onboardingEmailLogRepository.save(onboardingEmailLog);
//        
//        try {
//            // Verify batch exists
//            EmployeeBatchLog employeeBatchLog = employeeBatchLogRepository.findById(employeeBatchLogId)
//                    .orElseThrow(() -> new RuntimeException("Employee batch log not found"));
//            
//            // Get ALL employees from database
//            List<Employee> allEmployees = employeeRepository.findAll();
//            onboardingEmailLog.setTotalEmployees(allEmployees.size());
//            onboardingEmailLogRepository.save(onboardingEmailLog);
//            
//            log.info("Processing {} employees for onboarding welcome emails", allEmployees.size());
//            
//            if (allEmployees.isEmpty()) {
//                onboardingEmailLog.setStatus("COMPLETED");
//                // Initialize empty JSON lists
//                onboardingEmailLog.setSentEmailsList("[]");
//                onboardingEmailLog.setFailedEmailsList("[]");
//                onboardingEmailLog.setIneligibleEmailsList("[]");
//                onboardingEmailLog.setDateErrorEmailsList("[]");
//                onboardingEmailLogRepository.save(onboardingEmailLog);
//                log.warn("No employees found in database");
//                return;
//            }
//            
//            // Initialize lists to store email details
//            List<String> sentEmails = new ArrayList<>();
//            List<String> failedEmails = new ArrayList<>();
//            List<String> ineligibleEmails = new ArrayList<>();
//            List<String> dateErrorEmails = new ArrayList<>();
//            
//            int emailsSent = 0;
//            int emailsFailed = 0;
//            int ineligibleDueToDate = 0;
//            int dateFormatErrors = 0;
//            
//            // Send welcome emails only to eligible employees
//            for (Employee employee : allEmployees) {
//                Boolean eligibility = isEligibleForOnboardingEmail(employee);
//                
//                if (eligibility == null) {
//                    // Date parse error
//                    dateFormatErrors++;
//                    String errorEntry = employee.getEmail() + " - " + employee.getJoinDate();
//                    dateErrorEmails.add(errorEntry);
//                    log.warn("Date format error for {}: {}", employee.getEmail(), employee.getJoinDate());
//                    
//                } else if (eligibility) {
//                    // Eligible - send email
//                    boolean emailSent = sendWelcomeEmail(employee);
//                    
//                    if (emailSent) {
//                        emailsSent++;
//                        sentEmails.add(employee.getEmail());
//                        log.info("Email sent to: {} (Joined: {})", employee.getEmail(), employee.getJoinDate());
//                    } else {
//                        emailsFailed++;
//                        failedEmails.add(employee.getEmail());
//                        log.error("Failed to send email to: {}", employee.getEmail());
//                    }
//                } else {
//                    // Not eligible due to join date
//                    ineligibleDueToDate++;
//                    String ineligibleEntry = employee.getEmail() + " - " + employee.getJoinDate();
//                    ineligibleEmails.add(ineligibleEntry);
//                    log.info("Ineligible due to join date: {} (Joined: {})", employee.getEmail(), employee.getJoinDate());
//                }
//            }
//            
//            // Save all lists as JSON
//            onboardingEmailLog.setSentEmailsList(objectMapper.writeValueAsString(sentEmails));
//            onboardingEmailLog.setFailedEmailsList(objectMapper.writeValueAsString(failedEmails));
//            onboardingEmailLog.setIneligibleEmailsList(objectMapper.writeValueAsString(ineligibleEmails));
//            onboardingEmailLog.setDateErrorEmailsList(objectMapper.writeValueAsString(dateErrorEmails));
//            
//            // Update counters
//            onboardingEmailLog.setEmailsSent(emailsSent);
//            onboardingEmailLog.setEmailsFailed(emailsFailed);
//            onboardingEmailLog.setIneligibleDueToDate(ineligibleDueToDate);
//            onboardingEmailLog.setDateFormatErrors(dateFormatErrors);
//            onboardingEmailLog.setStatus("COMPLETED");
//            onboardingEmailLog.setCompletedAt(LocalDateTime.now());
//            
//            onboardingEmailLogRepository.save(onboardingEmailLog);
//            
//            log.info("Onboarding email sending completed for batch {}", employeeBatchLogId);
//            log.info("Final Report - Total: {}, Sent: {}, Failed: {}, Ineligible: {}, Date Errors: {}", 
//                    allEmployees.size(), emailsSent, emailsFailed, ineligibleDueToDate, dateFormatErrors);
//            
//        } catch (Exception e) {
//            log.error("Onboarding email sending failed for batch: {}", employeeBatchLogId, e);
//            // Update status to failed
//            try {
//                OnboardingEmailLog failedLog = onboardingEmailLogRepository.findById(onboardingEmailLog.getId()).orElse(null);
//                if (failedLog != null) {
//                    failedLog.setStatus("FAILED");
//                    onboardingEmailLogRepository.save(failedLog);
//                }
//            } catch (Exception ex) {
//                log.error("Failed to update email log status: {}", ex.getMessage());
//            }
//        }
//    }
//    
//    /**
//     * Enhanced eligibility check that handles multiple date formats
//     * @return Boolean - true if eligible, false if not eligible, null if date parsing failed
//     */
//    private Boolean isEligibleForOnboardingEmail(Employee employee) {
//        // Basic validation
//        if (!isValidEmployeeForEmail(employee)) {
//            log.debug("Basic validation failed for employee: {}", employee.getEmail());
//            return false;
//        }
//        
//        // Join date validation
//        if (employee.getJoinDate() == null || employee.getJoinDate().trim().isEmpty()) {
//            log.warn("Employee {} has no join date", employee.getEmail());
//            return false;
//        }
//        
//        try {
//            LocalDate joinDate = parseJoinDate(employee.getJoinDate());
//            if (joinDate == null) {
//                log.error("Could not parse join date for {}: {}", employee.getEmail(), employee.getJoinDate());
//                return null;
//            }
//            
//            LocalDate currentDate = LocalDate.now();
//            
//            // Calculate days since joining
//            long daysSinceJoining = ChronoUnit.DAYS.between(joinDate, currentDate);
//            
//            log.debug("Employee {} joined on {} ({} days ago)", 
//                     employee.getEmail(), joinDate, daysSinceJoining);
//            
//            // Check eligibility criteria
//            if (daysSinceJoining < 0) {
//                log.warn("Employee {} has future join date: {} ({} days in future)", 
//                        employee.getEmail(), joinDate, Math.abs(daysSinceJoining));
//                return false;
//            }
//            
//            if (daysSinceJoining <= MAX_DAYS_SINCE_JOINING) {
//                log.info("ELIGIBLE: {} - joined {} days ago (Join Date: {})", 
//                        employee.getEmail(), daysSinceJoining, employee.getJoinDate());
//                return true;
//            } else {
//                log.info("INELIGIBLE: {} - joined {} days ago (> {} days limit) (Join Date: {})", 
//                        employee.getEmail(), daysSinceJoining, MAX_DAYS_SINCE_JOINING, employee.getJoinDate());
//                return false;
//            }
//            
//        } catch (Exception e) {
//            log.error("Unexpected error parsing date for {}: {}", employee.getEmail(), e.getMessage());
//            return null;
//        }
//    }
//    
//    /**
//     * Smart date parser that tries multiple formats
//     */
//    private LocalDate parseJoinDate(String joinDateString) {
//        if (joinDateString == null || joinDateString.trim().isEmpty()) {
//            return null;
//        }
//        
//        String trimmedDate = joinDateString.trim();
//        
//        // Try each date format until one works
//        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
//            try {
//                LocalDate parsedDate = LocalDate.parse(trimmedDate, formatter);
//                log.debug("Successfully parsed date '{}' using format: {}", trimmedDate, formatter.toString());
//                return parsedDate;
//            } catch (DateTimeParseException e) {
//                // Try next format - this is expected behavior
//                continue;
//            }
//        }
//        
//        // If none of the formats worked, log the available formats
//        log.warn("Could not parse date '{}'. Supported formats:", trimmedDate);
//        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
//            log.warn("   - {}", formatter.toString().replace("ParseCaseSensitive(true)", ""));
//        }
//        
//        return null;
//    }
//    
//    private boolean sendWelcomeEmail(Employee employee) {
//        try {
//            org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
//            context.setVariable("employeeName", employee.getName());
//            context.setVariable("employeeId", employee.getEmployeeId());
//            context.setVariable("joinDate", employee.getJoinDate());
//            context.setVariable("currentDate", LocalDateTime.now());
//            
//            emailService.sendEmailWithTemplate(
//                employee.getEmail(), 
//                "Welcome to Our Company - " + employee.getName(),
//                "onboarding-welcome",
//                context
//            );
//            
//            return true;
//            
//        } catch (Exception e) {
//            log.error("Failed to send email to {}: {}", employee.getEmail(), e.getMessage());
//            return false;
//        }
//    }
//    
//    private boolean isValidEmployeeForEmail(Employee employee) {
//        return employee.getEmail() != null && 
//               !employee.getEmail().isEmpty() &&
//               employee.getName() != null &&
//               !employee.getName().isEmpty();
//    }
//    
//    @Override
//    public Boolean isEmployeeEligibleForOnboarding(String employeeEmail) {
//        try {
//            Employee employee = employeeRepository.findByEmail(employeeEmail).orElse(null);
//            if (employee == null) {
//                log.warn("Employee not found with email: {}", employeeEmail);
//                return false;
//            }
//            return isEligibleForOnboardingEmail(employee);
//        } catch (Exception e) {
//            log.error("Error checking eligibility for {}: {}", employeeEmail, e.getMessage());
//            return false;
//        }
//    }
//    
//    @Override
//    public Map<String, Object> getEmailResults(Long logId) {
//        try {
//            OnboardingEmailLog emailLog = onboardingEmailLogRepository.findById(logId).orElse(null);
//            if (emailLog == null) {
//                return Collections.singletonMap("error", "Log not found");
//            }
//            
//            Map<String, Object> results = new HashMap<>();
//            results.put("totalEmployees", emailLog.getTotalEmployees());
//            results.put("emailsSent", emailLog.getEmailsSent());
//            results.put("emailsFailed", emailLog.getEmailsFailed());
//            results.put("ineligibleDueToDate", emailLog.getIneligibleDueToDate());
//            results.put("dateFormatErrors", emailLog.getDateFormatErrors());
//            results.put("status", emailLog.getStatus());
//            results.put("startedAt", emailLog.getStartedAt());
//            results.put("completedAt", emailLog.getCompletedAt());
//            
//            // Parse JSON lists
//            try {
//                results.put("sentEmails", objectMapper.readValue(emailLog.getSentEmailsList(), List.class));
//                results.put("failedEmails", objectMapper.readValue(emailLog.getFailedEmailsList(), List.class));
//                results.put("ineligibleEmails", objectMapper.readValue(emailLog.getIneligibleEmailsList(), List.class));
//                results.put("dateErrorEmails", objectMapper.readValue(emailLog.getDateErrorEmailsList(), List.class));
//            } catch (Exception e) {
//                log.warn("Error parsing JSON lists for log {}: {}", logId, e.getMessage());
//                results.put("sentEmails", Collections.emptyList());
//                results.put("failedEmails", Collections.emptyList());
//                results.put("ineligibleEmails", Collections.emptyList());
//                results.put("dateErrorEmails", Collections.emptyList());
//            }
//            
//            return results;
//            
//        } catch (Exception e) {
//            log.error("Error getting email results: {}", e.getMessage());
//            return Collections.singletonMap("error", e.getMessage());
//        }
//    }
//    
//    @Override
//    public List<OnboardingEmailLog> getAllEmailLogs() {
//        try {
//            return onboardingEmailLogRepository.findAllByOrderByStartedAtDesc();
//        } catch (Exception e) {
//            log.error("Error getting all email logs: {}", e.getMessage());
//            return Collections.emptyList();
//        }
//    }
//
//    @Override
//    public Map<String, Object> getOnboardingStatistics() {
//        try {
//            List<OnboardingEmailLog> allLogs = onboardingEmailLogRepository.findAll();
//            
//            long totalBatches = allLogs.size();
//            long completedBatches = allLogs.stream().filter(log -> "COMPLETED".equals(log.getStatus())).count();
//            long failedBatches = allLogs.stream().filter(log -> "FAILED".equals(log.getStatus())).count();
//            long totalEmailsSent = allLogs.stream().mapToInt(OnboardingEmailLog::getEmailsSent).sum();
//            long totalEmailsFailed = allLogs.stream().mapToInt(OnboardingEmailLog::getEmailsFailed).sum();
//            long pendingBatches = allLogs.stream().filter(log -> "PROCESSING".equals(log.getStatus())).count();
//            
//            double successRate = totalBatches > 0 ? (double) completedBatches / totalBatches * 100 : 0;
//            double emailSuccessRate = (totalEmailsSent + totalEmailsFailed) > 0 ? 
//                (double) totalEmailsSent / (totalEmailsSent + totalEmailsFailed) * 100 : 0;
//            
//            return Map.of(
//                "totalOnboardingBatches", totalBatches,
//                "completedBatches", completedBatches,
//                "failedBatches", failedBatches,
//                "pendingBatches", pendingBatches,
//                "totalEmailsSent", totalEmailsSent,
//                "totalEmailsFailed", totalEmailsFailed,
//                "batchSuccessRate", String.format("%.1f%%", successRate),
//                "emailSuccessRate", String.format("%.1f%%", emailSuccessRate),
//                "lastUpdated", LocalDateTime.now()
//            );
//        } catch (Exception e) {
//            log.error("Error getting onboarding statistics: {}", e.getMessage());
//            return Map.of(
//                "totalOnboardingBatches", 0,
//                "completedBatches", 0,
//                "failedBatches", 0,
//                "pendingBatches", 0,
//                "totalEmailsSent", 0,
//                "totalEmailsFailed", 0,
//                "batchSuccessRate", "0%",
//                "emailSuccessRate", "0%",
//                "error", e.getMessage()
//            );
//        }
//    }
//    
//    @Override
//    public List<OnboardingEmailLog> getEmailLogsByBatchId(Long batchId) {
//        try {
//            return onboardingEmailLogRepository.findByEmployeeBatchLogIdOrderByStartedAtDesc(batchId);
//        } catch (Exception e) {
//            log.error("Error getting email logs for batch {}: {}", batchId, e.getMessage());
//            return Collections.emptyList();
//        }
//    }
//    
//    @Async
//    @Override
//    public void resendFailedEmails(Long logId) {
//        try {
//            OnboardingEmailLog originalLog = onboardingEmailLogRepository.findById(logId)
//                    .orElseThrow(() -> new RuntimeException("Email log not found"));
//            
//            // Create new log for resend attempt
//            OnboardingEmailLog resendLog = new OnboardingEmailLog();
//            resendLog.setEmployeeBatchLogId(originalLog.getEmployeeBatchLogId());
//            resendLog.setStatus("PROCESSING");
//            resendLog.setTotalEmployees(originalLog.getEmailsFailed());
//            resendLog = onboardingEmailLogRepository.save(resendLog);
//            
//            try {
//                List<String> failedEmails = objectMapper.readValue(originalLog.getFailedEmailsList(), List.class);
//                List<String> resentEmails = new ArrayList<>();
//                List<String> stillFailedEmails = new ArrayList<>();
//                
//                int resentCount = 0;
//                int stillFailedCount = 0;
//                
//                for (String email : failedEmails) {
//                    Employee employee = employeeRepository.findByEmail(email).orElse(null);
//                    if (employee != null) {
//                        boolean emailSent = sendWelcomeEmail(employee);
//                        if (emailSent) {
//                            resentCount++;
//                            resentEmails.add(email);
//                        } else {
//                            stillFailedCount++;
//                            stillFailedEmails.add(email);
//                        }
//                    }
//                }
//                
//                // Update resend log
//                resendLog.setSentEmailsList(objectMapper.writeValueAsString(resentEmails));
//                resendLog.setFailedEmailsList(objectMapper.writeValueAsString(stillFailedEmails));
//                resendLog.setIneligibleEmailsList("[]");
//                resendLog.setDateErrorEmailsList("[]");
//                resendLog.setEmailsSent(resentCount);
//                resendLog.setEmailsFailed(stillFailedCount);
//                resendLog.setIneligibleDueToDate(0);
//                resendLog.setDateFormatErrors(0);
//                resendLog.setStatus("COMPLETED");
//                resendLog.setCompletedAt(LocalDateTime.now());
//                
//                onboardingEmailLogRepository.save(resendLog);
//                
//                log.info("Resend completed: {} emails resent, {} still failed", resentCount, stillFailedCount);
//                
//            } catch (Exception e) {
//                resendLog.setStatus("FAILED");
//                onboardingEmailLogRepository.save(resendLog);
//                throw e;
//            }
//            
//        } catch (Exception e) {
//            log.error("Failed to resend emails for log {}: {}", logId, e.getMessage());
//        }
//    }
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//package com.survey.serviceImpl;
//
//import com.survey.entity.OnboardingEmailLog;
//import com.survey.entity.Employee;
//import com.survey.entity.EmployeeBatchLog;
//import com.survey.repository.OnboardingEmailLogRepository;
//import com.survey.repository.EmployeeBatchLogRepository;
//import com.survey.repository.EmployeeRepository;
//import com.survey.service.OnboardingEmailService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import java.time.LocalDateTime;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class OnboardingEmailServiceImpl implements OnboardingEmailService {
//
//    private final EmployeeRepository employeeRepository;
//    private final EmployeeBatchLogRepository employeeBatchLogRepository;
//    private final OnboardingEmailLogRepository onboardingEmailLogRepository;
//    private final EmailService emailService;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    
//    // Multiple date formats to handle different input sources
//    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
//        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
//        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
//        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
//        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
//        DateTimeFormatter.ofPattern("MM-dd-yyyy"),
//        DateTimeFormatter.ofPattern("dd/MM/yyyy")
//    );
//    
//    private static final int MAX_DAYS_SINCE_JOINING = 15;
//
//    @Async
//    @Override
//    public void sendWelcomeEmailsForBatch(Long employeeBatchLogId) {
//        log.info("Starting onboarding welcome emails for batch: {}", employeeBatchLogId);
//        
//        // ✅ CHECK if emails have already been sent for this batch using OnboardingEmailLogRepository
//        OnboardingEmailLog existingLog = onboardingEmailLogRepository.findByEmployeeBatchLogId(employeeBatchLogId);
//        if (existingLog != null && "COMPLETED".equals(existingLog.getStatus())) {
//            log.info("Emails have already been sent for batch {}. Skipping...", employeeBatchLogId);
//            return;
//        }
//        
//        // Create email log entry
//        OnboardingEmailLog onboardingEmailLog = new OnboardingEmailLog();
//        onboardingEmailLog.setEmployeeBatchLogId(employeeBatchLogId);
//        onboardingEmailLog.setStatus("PROCESSING");
//        onboardingEmailLog = onboardingEmailLogRepository.save(onboardingEmailLog);
//        
//        try {
//            // Verify batch exists
//            EmployeeBatchLog employeeBatchLog = employeeBatchLogRepository.findById(employeeBatchLogId)
//                    .orElseThrow(() -> new RuntimeException("Employee batch log not found"));
//            
//            // Get ALL employees from database
//            List<Employee> allEmployees = employeeRepository.findAll();
//            onboardingEmailLog.setTotalEmployees(allEmployees.size());
//            onboardingEmailLogRepository.save(onboardingEmailLog);
//            
//            log.info("Processing {} employees for onboarding welcome emails", allEmployees.size());
//            
//            if (allEmployees.isEmpty()) {
//                onboardingEmailLog.setStatus("COMPLETED");
//                // Initialize empty JSON lists
//                onboardingEmailLog.setSentEmailsList("[]");
//                onboardingEmailLog.setFailedEmailsList("[]");
//                onboardingEmailLog.setIneligibleEmailsList("[]");
//                onboardingEmailLog.setDateErrorEmailsList("[]");
//                onboardingEmailLogRepository.save(onboardingEmailLog);
//                log.warn("No employees found in database");
//                return;
//            }
//            
//            // Initialize lists to store email details
//            List<String> sentEmails = new ArrayList<>();
//            List<String> failedEmails = new ArrayList<>();
//            List<String> ineligibleEmails = new ArrayList<>();
//            List<String> dateErrorEmails = new ArrayList<>();
//            
//            int emailsSent = 0;
//            int emailsFailed = 0;
//            int ineligibleDueToDate = 0;
//            int dateFormatErrors = 0;
//            
//            // Send welcome emails only to eligible employees
//            for (Employee employee : allEmployees) {
//                Boolean eligibility = isEligibleForOnboardingEmail(employee);
//                
//                if (eligibility == null) {
//                    // Date parse error
//                    dateFormatErrors++;
//                    String errorEntry = employee.getEmail() + " - " + employee.getJoinDate();
//                    dateErrorEmails.add(errorEntry);
//                    log.warn("Date format error for {}: {}", employee.getEmail(), employee.getJoinDate());
//                    
//                } else if (eligibility) {
//                    // Eligible - send email
//                    boolean emailSent = sendWelcomeEmail(employee);
//                    
//                    if (emailSent) {
//                        emailsSent++;
//                        sentEmails.add(employee.getEmail());
//                        log.info("Email sent to: {} (Joined: {})", employee.getEmail(), employee.getJoinDate());
//                    } else {
//                        emailsFailed++;
//                        failedEmails.add(employee.getEmail());
//                        log.error("Failed to send email to: {}", employee.getEmail());
//                    }
//                } else {
//                    // Not eligible due to join date
//                    ineligibleDueToDate++;
//                    String ineligibleEntry = employee.getEmail() + " - " + employee.getJoinDate();
//                    ineligibleEmails.add(ineligibleEntry);
//                    log.info("Ineligible due to join date: {} (Joined: {})", employee.getEmail(), employee.getJoinDate());
//                }
//            }
//            
//            // Save all lists as JSON
//            onboardingEmailLog.setSentEmailsList(objectMapper.writeValueAsString(sentEmails));
//            onboardingEmailLog.setFailedEmailsList(objectMapper.writeValueAsString(failedEmails));
//            onboardingEmailLog.setIneligibleEmailsList(objectMapper.writeValueAsString(ineligibleEmails));
//            onboardingEmailLog.setDateErrorEmailsList(objectMapper.writeValueAsString(dateErrorEmails));
//            
//            // Update counters
//            onboardingEmailLog.setEmailsSent(emailsSent);
//            onboardingEmailLog.setEmailsFailed(emailsFailed);
//            onboardingEmailLog.setIneligibleDueToDate(ineligibleDueToDate);
//            onboardingEmailLog.setDateFormatErrors(dateFormatErrors);
//            onboardingEmailLog.setStatus("COMPLETED");
//            onboardingEmailLog.setCompletedAt(LocalDateTime.now());
//            
//            onboardingEmailLogRepository.save(onboardingEmailLog);
//            
//            log.info("Onboarding email sending completed for batch {}", employeeBatchLogId);
//            log.info("Final Report - Total: {}, Sent: {}, Failed: {}, Ineligible: {}, Date Errors: {}", 
//                    allEmployees.size(), emailsSent, emailsFailed, ineligibleDueToDate, dateFormatErrors);
//            
//        } catch (Exception e) {
//            log.error("Onboarding email sending failed for batch: {}", employeeBatchLogId, e);
//            // Update status to failed
//            try {
//                OnboardingEmailLog failedLog = onboardingEmailLogRepository.findById(onboardingEmailLog.getId()).orElse(null);
//                if (failedLog != null) {
//                    failedLog.setStatus("FAILED");
//                    onboardingEmailLogRepository.save(failedLog);
//                }
//            } catch (Exception ex) {
//                log.error("Failed to update email log status: {}", ex.getMessage());
//            }
//        }
//    }
//    
//    /**
//     * Enhanced eligibility check that handles multiple date formats
//     * @return Boolean - true if eligible, false if not eligible, null if date parsing failed
//     */
//    private Boolean isEligibleForOnboardingEmail(Employee employee) {
//        // Basic validation
//        if (!isValidEmployeeForEmail(employee)) {
//            log.debug("Basic validation failed for employee: {}", employee.getEmail());
//            return false;
//        }
//        
//        // Join date validation
//        if (employee.getJoinDate() == null || employee.getJoinDate().trim().isEmpty()) {
//            log.warn("Employee {} has no join date", employee.getEmail());
//            return false;
//        }
//        
//        try {
//            LocalDate joinDate = parseJoinDate(employee.getJoinDate());
//            if (joinDate == null) {
//                log.error("Could not parse join date for {}: {}", employee.getEmail(), employee.getJoinDate());
//                return null;
//            }
//            
//            LocalDate currentDate = LocalDate.now();
//            
//            // Calculate days since joining
//            long daysSinceJoining = ChronoUnit.DAYS.between(joinDate, currentDate);
//            
//            log.debug("Employee {} joined on {} ({} days ago)", 
//                     employee.getEmail(), joinDate, daysSinceJoining);
//            
//            // Check eligibility criteria
//            if (daysSinceJoining < 0) {
//                log.warn("Employee {} has future join date: {} ({} days in future)", 
//                        employee.getEmail(), joinDate, Math.abs(daysSinceJoining));
//                return false;
//            }
//            
//            if (daysSinceJoining <= MAX_DAYS_SINCE_JOINING) {
//                log.info("ELIGIBLE: {} - joined {} days ago (Join Date: {})", 
//                        employee.getEmail(), daysSinceJoining, employee.getJoinDate());
//                return true;
//            } else {
//                log.info("INELIGIBLE: {} - joined {} days ago (> {} days limit) (Join Date: {})", 
//                        employee.getEmail(), daysSinceJoining, MAX_DAYS_SINCE_JOINING, employee.getJoinDate());
//                return false;
//            }
//            
//        } catch (Exception e) {
//            log.error("Unexpected error parsing date for {}: {}", employee.getEmail(), e.getMessage());
//            return null;
//        }
//    }
//    
//    /**
//     * Smart date parser that tries multiple formats
//     */
//    private LocalDate parseJoinDate(String joinDateString) {
//        if (joinDateString == null || joinDateString.trim().isEmpty()) {
//            return null;
//        }
//        
//        String trimmedDate = joinDateString.trim();
//        
//        // Try each date format until one works
//        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
//            try {
//                LocalDate parsedDate = LocalDate.parse(trimmedDate, formatter);
//                log.debug("Successfully parsed date '{}' using format: {}", trimmedDate, formatter.toString());
//                return parsedDate;
//            } catch (DateTimeParseException e) {
//                // Try next format - this is expected behavior
//                continue;
//            }
//        }
//        
//        // If none of the formats worked, log the available formats
//        log.warn("Could not parse date '{}'. Supported formats:", trimmedDate);
//        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
//            log.warn("   - {}", formatter.toString().replace("ParseCaseSensitive(true)", ""));
//        }
//        
//        return null;
//    }
//    
//    private boolean sendWelcomeEmail(Employee employee) {
//        try {
//            // Create context with employee data for the template
//            org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
//            context.setVariable("employeeName", employee.getName());
//            context.setVariable("employeeId", employee.getEmployeeId());
//            context.setVariable("employeeEmail", employee.getEmail());
//            context.setVariable("joinDate", employee.getJoinDate());
//            context.setVariable("currentDate", LocalDateTime.now());
//            
//            // Use your existing EmailService to send the email
//            emailService.sendEmailWithTemplate(
//                employee.getEmail(), 
//                "Welcome to Aurionpro - " + employee.getName(),
//                "onboarding-welcome", // This should match your Thymeleaf template name
//                context
//            );
//            
//            return true;
//            
//        } catch (Exception e) {
//            log.error("Failed to send email to {}: {}", employee.getEmail(), e.getMessage());
//            return false;
//        }
//    }
//    
//    private boolean isValidEmployeeForEmail(Employee employee) {
//        return employee.getEmail() != null && 
//               !employee.getEmail().isEmpty() &&
//               employee.getName() != null &&
//               !employee.getName().isEmpty();
//    }
//    
//    @Override
//    public Boolean isEmployeeEligibleForOnboarding(String employeeEmail) {
//        try {
//            Employee employee = employeeRepository.findByEmail(employeeEmail).orElse(null);
//            if (employee == null) {
//                log.warn("Employee not found with email: {}", employeeEmail);
//                return false;
//            }
//            return isEligibleForOnboardingEmail(employee);
//        } catch (Exception e) {
//            log.error("Error checking eligibility for {}: {}", employeeEmail, e.getMessage());
//            return false;
//        }
//    }
//    
//    @Override
//    public Map<String, Object> getEmailResults(Long logId) {
//        try {
//            OnboardingEmailLog emailLog = onboardingEmailLogRepository.findById(logId).orElse(null);
//            if (emailLog == null) {
//                return Collections.singletonMap("error", "Log not found");
//            }
//            
//            Map<String, Object> results = new HashMap<>();
//            results.put("totalEmployees", emailLog.getTotalEmployees());
//            results.put("emailsSent", emailLog.getEmailsSent());
//            results.put("emailsFailed", emailLog.getEmailsFailed());
//            results.put("ineligibleDueToDate", emailLog.getIneligibleDueToDate());
//            results.put("dateFormatErrors", emailLog.getDateFormatErrors());
//            results.put("status", emailLog.getStatus());
//            results.put("startedAt", emailLog.getStartedAt());
//            results.put("completedAt", emailLog.getCompletedAt());
//            
//            // Parse JSON lists
//            try {
//                results.put("sentEmails", objectMapper.readValue(emailLog.getSentEmailsList(), List.class));
//                results.put("failedEmails", objectMapper.readValue(emailLog.getFailedEmailsList(), List.class));
//                results.put("ineligibleEmails", objectMapper.readValue(emailLog.getIneligibleEmailsList(), List.class));
//                results.put("dateErrorEmails", objectMapper.readValue(emailLog.getDateErrorEmailsList(), List.class));
//            } catch (Exception e) {
//                log.warn("Error parsing JSON lists for log {}: {}", logId, e.getMessage());
//                results.put("sentEmails", Collections.emptyList());
//                results.put("failedEmails", Collections.emptyList());
//                results.put("ineligibleEmails", Collections.emptyList());
//                results.put("dateErrorEmails", Collections.emptyList());
//            }
//            
//            return results;
//            
//        } catch (Exception e) {
//            log.error("Error getting email results: {}", e.getMessage());
//            return Collections.singletonMap("error", e.getMessage());
//        }
//    }
//    
//    @Override
//    public List<OnboardingEmailLog> getAllEmailLogs() {
//        try {
//            return onboardingEmailLogRepository.findAllByOrderByStartedAtDesc();
//        } catch (Exception e) {
//            log.error("Error getting all email logs: {}", e.getMessage());
//            return Collections.emptyList();
//        }
//    }
//
//    @Override
//    public Map<String, Object> getOnboardingStatistics() {
//        try {
//            List<OnboardingEmailLog> allLogs = onboardingEmailLogRepository.findAll();
//            
//            long totalBatches = allLogs.size();
//            long completedBatches = allLogs.stream().filter(log -> "COMPLETED".equals(log.getStatus())).count();
//            long failedBatches = allLogs.stream().filter(log -> "FAILED".equals(log.getStatus())).count();
//            long totalEmailsSent = allLogs.stream().mapToInt(OnboardingEmailLog::getEmailsSent).sum();
//            long totalEmailsFailed = allLogs.stream().mapToInt(OnboardingEmailLog::getEmailsFailed).sum();
//            long pendingBatches = allLogs.stream().filter(log -> "PROCESSING".equals(log.getStatus())).count();
//            
//            double successRate = totalBatches > 0 ? (double) completedBatches / totalBatches * 100 : 0;
//            double emailSuccessRate = (totalEmailsSent + totalEmailsFailed) > 0 ? 
//                (double) totalEmailsSent / (totalEmailsSent + totalEmailsFailed) * 100 : 0;
//            
//            return Map.of(
//                "totalOnboardingBatches", totalBatches,
//                "completedBatches", completedBatches,
//                "failedBatches", failedBatches,
//                "pendingBatches", pendingBatches,
//                "totalEmailsSent", totalEmailsSent,
//                "totalEmailsFailed", totalEmailsFailed,
//                "batchSuccessRate", String.format("%.1f%%", successRate),
//                "emailSuccessRate", String.format("%.1f%%", emailSuccessRate),
//                "lastUpdated", LocalDateTime.now()
//            );
//        } catch (Exception e) {
//            log.error("Error getting onboarding statistics: {}", e.getMessage());
//            return Map.of(
//                "totalOnboardingBatches", 0,
//                "completedBatches", 0,
//                "failedBatches", 0,
//                "pendingBatches", 0,
//                "totalEmailsSent", 0,
//                "totalEmailsFailed", 0,
//                "batchSuccessRate", "0%",
//                "emailSuccessRate", "0%",
//                "error", e.getMessage()
//            );
//        }
//    }
//    
//    @Override
//    public List<OnboardingEmailLog> getEmailLogsByBatchId(Long batchId) {
//        try {
//            return onboardingEmailLogRepository.findByEmployeeBatchLogIdOrderByStartedAtDesc(batchId);
//        } catch (Exception e) {
//            log.error("Error getting email logs for batch {}: {}", batchId, e.getMessage());
//            return Collections.emptyList();
//        }
//    }
//    
//    @Async
//    @Override
//    public void resendFailedEmails(Long logId) {
//        try {
//            OnboardingEmailLog originalLog = onboardingEmailLogRepository.findById(logId)
//                    .orElseThrow(() -> new RuntimeException("Email log not found"));
//            
//            // Create new log for resend attempt
//            OnboardingEmailLog resendLog = new OnboardingEmailLog();
//            resendLog.setEmployeeBatchLogId(originalLog.getEmployeeBatchLogId());
//            resendLog.setStatus("PROCESSING");
//            resendLog.setTotalEmployees(originalLog.getEmailsFailed());
//            resendLog = onboardingEmailLogRepository.save(resendLog);
//            
//            try {
//                List<String> failedEmails = objectMapper.readValue(originalLog.getFailedEmailsList(), List.class);
//                List<String> resentEmails = new ArrayList<>();
//                List<String> stillFailedEmails = new ArrayList<>();
//                
//                int resentCount = 0;
//                int stillFailedCount = 0;
//                
//                for (String email : failedEmails) {
//                    Employee employee = employeeRepository.findByEmail(email).orElse(null);
//                    if (employee != null) {
//                        boolean emailSent = sendWelcomeEmail(employee);
//                        if (emailSent) {
//                            resentCount++;
//                            resentEmails.add(email);
//                        } else {
//                            stillFailedCount++;
//                            stillFailedEmails.add(email);
//                        }
//                    }
//                }
//                
//                // Update resend log
//                resendLog.setSentEmailsList(objectMapper.writeValueAsString(resentEmails));
//                resendLog.setFailedEmailsList(objectMapper.writeValueAsString(stillFailedEmails));
//                resendLog.setIneligibleEmailsList("[]");
//                resendLog.setDateErrorEmailsList("[]");
//                resendLog.setEmailsSent(resentCount);
//                resendLog.setEmailsFailed(stillFailedCount);
//                resendLog.setIneligibleDueToDate(0);
//                resendLog.setDateFormatErrors(0);
//                resendLog.setStatus("COMPLETED");
//                resendLog.setCompletedAt(LocalDateTime.now());
//                
//                onboardingEmailLogRepository.save(resendLog);
//                
//                log.info("Resend completed: {} emails resent, {} still failed", resentCount, stillFailedCount);
//                
//            } catch (Exception e) {
//                resendLog.setStatus("FAILED");
//                onboardingEmailLogRepository.save(resendLog);
//                throw e;
//            }
//            
//        } catch (Exception e) {
//            log.error("Failed to resend emails for log {}: {}", logId, e.getMessage());
//        }
//    }
//}
//
//





































package com.survey.serviceImpl;

import com.survey.entity.OnboardingEmailLog;
import com.survey.entity.Employee;
import com.survey.entity.EmployeeBatchLog;
import com.survey.repository.OnboardingEmailLogRepository;
import com.survey.repository.EmployeeBatchLogRepository;
import com.survey.repository.EmployeeRepository;
import com.survey.service.OnboardingEmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OnboardingEmailServiceImpl implements OnboardingEmailService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeBatchLogRepository employeeBatchLogRepository;
    private final OnboardingEmailLogRepository onboardingEmailLogRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Multiple date formats to handle different input sources
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("MM-dd-yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy")
    );
    
    private static final int MAX_DAYS_SINCE_JOINING = 15;

    @Async
    @Override
    public void sendWelcomeEmailsForBatch(Long employeeBatchLogId) {
        log.info("Starting onboarding welcome emails for batch: {}", employeeBatchLogId);
        
        // CHECK if emails have already been sent for this batch using OnboardingEmailLogRepository
        OnboardingEmailLog existingLog = onboardingEmailLogRepository.findByEmployeeBatchLogId(employeeBatchLogId);
        if (existingLog != null && "COMPLETED".equals(existingLog.getStatus())) {
            log.info("Emails have already been sent for batch {}. Skipping...", employeeBatchLogId);
            return;
        }
        
        // Create email log entry
        OnboardingEmailLog onboardingEmailLog = new OnboardingEmailLog();
        onboardingEmailLog.setEmployeeBatchLogId(employeeBatchLogId);
        onboardingEmailLog.setStatus("PROCESSING");
        onboardingEmailLog = onboardingEmailLogRepository.save(onboardingEmailLog);
        
        try {
            // Verify batch exists
            EmployeeBatchLog employeeBatchLog = employeeBatchLogRepository.findById(employeeBatchLogId)
                    .orElseThrow(() -> new RuntimeException("Employee batch log not found"));
            
            // Get ALL employees from database
            List<Employee> allEmployees = employeeRepository.findAll();
            onboardingEmailLog.setTotalEmployees(allEmployees.size());
            onboardingEmailLogRepository.save(onboardingEmailLog);
            
            log.info("Processing {} employees for onboarding welcome emails", allEmployees.size());
            
            if (allEmployees.isEmpty()) {
                onboardingEmailLog.setStatus("COMPLETED");
                onboardingEmailLog.setSentEmailsList("[]");
                onboardingEmailLog.setFailedEmailsList("[]");
                onboardingEmailLog.setIneligibleEmailsList("[]");
                onboardingEmailLog.setDateErrorEmailsList("[]");
                onboardingEmailLogRepository.save(onboardingEmailLog);
                log.warn("No employees found in database");
                return;
            }
            
            List<String> sentEmails = new ArrayList<>();
            List<String> failedEmails = new ArrayList<>();
            List<String> ineligibleEmails = new ArrayList<>();
            List<String> dateErrorEmails = new ArrayList<>();
            
            int emailsSent = 0;
            int emailsFailed = 0;
            int ineligibleDueToDate = 0;
            int dateFormatErrors = 0;
            
            for (Employee employee : allEmployees) {
                Boolean eligibility = isEligibleForOnboardingEmail(employee);
                
                if (eligibility == null) {
                    dateFormatErrors++;
                    String errorEntry = employee.getEmail() + " - " + employee.getJoinDate();
                    dateErrorEmails.add(errorEntry);
                    log.warn("Date format error for {}: {}", employee.getEmail(), employee.getJoinDate());
                    
                } else if (eligibility) {
                    boolean emailSent = sendWelcomeEmail(employee);
                    
                    if (emailSent) {
                        emailsSent++;
                        sentEmails.add(employee.getEmail());
                        log.info("Email sent to: {} (Joined: {})", employee.getEmail(), employee.getJoinDate());
                    } else {
                        emailsFailed++;
                        failedEmails.add(employee.getEmail());
                        log.error("Failed to send email to: {}", employee.getEmail());
                    }
                } else {
                    ineligibleDueToDate++;
                    String ineligibleEntry = employee.getEmail() + " - " + employee.getJoinDate();
                    ineligibleEmails.add(ineligibleEntry);
                    log.info("Ineligible due to join date: {} (Joined: {})", employee.getEmail(), employee.getJoinDate());
                }
            }
            
            onboardingEmailLog.setSentEmailsList(objectMapper.writeValueAsString(sentEmails));
            onboardingEmailLog.setFailedEmailsList(objectMapper.writeValueAsString(failedEmails));
            onboardingEmailLog.setIneligibleEmailsList(objectMapper.writeValueAsString(ineligibleEmails));
            onboardingEmailLog.setDateErrorEmailsList(objectMapper.writeValueAsString(dateErrorEmails));
            
            onboardingEmailLog.setEmailsSent(emailsSent);
            onboardingEmailLog.setEmailsFailed(emailsFailed);
            onboardingEmailLog.setIneligibleDueToDate(ineligibleDueToDate);
            onboardingEmailLog.setDateFormatErrors(dateFormatErrors);
            onboardingEmailLog.setStatus("COMPLETED");
            onboardingEmailLog.setCompletedAt(LocalDateTime.now());
            
            onboardingEmailLogRepository.save(onboardingEmailLog);
            
            log.info("Onboarding email sending completed for batch {}", employeeBatchLogId);
            
        } catch (Exception e) {
            log.error("Onboarding email sending failed for batch: {}", employeeBatchLogId, e);
            try {
                OnboardingEmailLog failedLog = onboardingEmailLogRepository.findById(onboardingEmailLog.getId()).orElse(null);
                if (failedLog != null) {
                    failedLog.setStatus("FAILED");
                    onboardingEmailLogRepository.save(failedLog);
                }
            } catch (Exception ex) {
                log.error("Failed to update email log status: {}", ex.getMessage());
            }
        }
    }
    
    private Boolean isEligibleForOnboardingEmail(Employee employee) {
        if (!isValidEmployeeForEmail(employee)) {
            log.debug("Basic validation failed for employee: {}", employee.getEmail());
            return false;
        }
        
        if (employee.getJoinDate() == null || employee.getJoinDate().trim().isEmpty()) {
            log.warn("Employee {} has no join date", employee.getEmail());
            return false;
        }
        
        try {
            LocalDate joinDate = parseJoinDate(employee.getJoinDate());
            if (joinDate == null) {
                log.error("Could not parse join date for {}: {}", employee.getEmail(), employee.getJoinDate());
                return null;
            }
            
            LocalDate currentDate = LocalDate.now();
            long daysSinceJoining = ChronoUnit.DAYS.between(joinDate, currentDate);
            
            if (daysSinceJoining < 0) {
                log.warn("Employee {} has future join date: {}", employee.getEmail(), joinDate);
                return false;
            }
            
            return daysSinceJoining <= MAX_DAYS_SINCE_JOINING;
            
        } catch (Exception e) {
            log.error("Unexpected error parsing date for {}: {}", employee.getEmail(), e.getMessage());
            return null;
        }
    }
    
    private LocalDate parseJoinDate(String joinDateString) {
        if (joinDateString == null || joinDateString.trim().isEmpty()) {
            return null;
        }
        
        String trimmedDate = joinDateString.trim();
        
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmedDate, formatter);
            } catch (DateTimeParseException e) {
                continue;
            }
        }
        
        log.warn("Could not parse date '{}'", trimmedDate);
        return null;
    }
    
//    private boolean sendWelcomeEmail(Employee employee) {
//        try {
//            org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
//            context.setVariable("employeeName", employee.getName());
//            context.setVariable("employeeId", employee.getEmployeeId());
//            context.setVariable("employeeEmail", employee.getEmail());
//            context.setVariable("joinDate", employee.getJoinDate());
//            context.setVariable("currentDate", LocalDateTime.now());
//
//            // ⭐ ONLY CHANGE YOU ASKED FOR → Add image path
//            //context.setVariable("welcomeImage", "https://via.placeholder.com/600x200/009B8C/FFFFFF?text=Welcome+to+Aurionpro");
//            //context.setVariable("welcomeImage", "https://i.ibb.co/your-image-code/welcome-banner.png");
//            
//            context.setVariable("welcomeImage", "https://i.ibb.co/v4wsyJp0/welcome-banner.png");
//            
//            emailService.sendEmailWithTemplate(
//                employee.getEmail(), 
//                "Welcome to Aurionpro - " + employee.getName(),
//                "onboarding-welcome",
//                context
//            );
//            
//            return true;
//            
//        } catch (Exception e) {
//            log.error("Failed to send email to {}: {}", employee.getEmail(), e.getMessage());
//            return false;
//        }
//    }
    
    private boolean sendWelcomeEmail(Employee employee) {
        try {
            org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
            context.setVariable("employeeName", employee.getName());
            context.setVariable("employeeId", employee.getEmployeeId());
            context.setVariable("employeeEmail", employee.getEmail());
            context.setVariable("joinDate", employee.getJoinDate());
            context.setVariable("currentDate", LocalDateTime.now());

            // ✅ CORRECT: Your ImgBB welcome banner URL
            context.setVariable("welcomeImage", "https://i.ibb.co/v4wsyJp0/welcome-banner.png");
            
            emailService.sendEmailWithTemplate(
                employee.getEmail(), 
                "Welcome to Aurionpro - " + employee.getName(),
                "onboarding-welcome",
                context
            );
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", employee.getEmail(), e.getMessage());
            return false;
        }
    }
    
    private boolean isValidEmployeeForEmail(Employee employee) {
        return employee.getEmail() != null && 
               !employee.getEmail().isEmpty() &&
               employee.getName() != null &&
               !employee.getName().isEmpty();
    }
    
    @Override
    public Boolean isEmployeeEligibleForOnboarding(String employeeEmail) {
        try {
            Employee employee = employeeRepository.findByEmail(employeeEmail).orElse(null);
            if (employee == null) {
                log.warn("Employee not found with email: {}", employeeEmail);
                return false;
            }
            return isEligibleForOnboardingEmail(employee);
        } catch (Exception e) {
            log.error("Error checking eligibility for {}: {}", employeeEmail, e.getMessage());
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getEmailResults(Long logId) {
        try {
            OnboardingEmailLog emailLog = onboardingEmailLogRepository.findById(logId).orElse(null);
            if (emailLog == null) {
                return Collections.singletonMap("error", "Log not found");
            }
            
            Map<String, Object> results = new HashMap<>();
            results.put("totalEmployees", emailLog.getTotalEmployees());
            results.put("emailsSent", emailLog.getEmailsSent());
            results.put("emailsFailed", emailLog.getEmailsFailed());
            results.put("ineligibleDueToDate", emailLog.getIneligibleDueToDate());
            results.put("dateFormatErrors", emailLog.getDateFormatErrors());
            results.put("status", emailLog.getStatus());
            results.put("startedAt", emailLog.getStartedAt());
            results.put("completedAt", emailLog.getCompletedAt());
            
            try {
                results.put("sentEmails", objectMapper.readValue(emailLog.getSentEmailsList(), List.class));
                results.put("failedEmails", objectMapper.readValue(emailLog.getFailedEmailsList(), List.class));
                results.put("ineligibleEmails", objectMapper.readValue(emailLog.getIneligibleEmailsList(), List.class));
                results.put("dateErrorEmails", objectMapper.readValue(emailLog.getDateErrorEmailsList(), List.class));
            } catch (Exception e) {
                results.put("sentEmails", Collections.emptyList());
                results.put("failedEmails", Collections.emptyList());
                results.put("ineligibleEmails", Collections.emptyList());
                results.put("dateErrorEmails", Collections.emptyList());
            }
            
            return results;
            
        } catch (Exception e) {
            log.error("Error getting email results: {}", e.getMessage());
            return Collections.singletonMap("error", e.getMessage());
        }
    }
    
    @Override
    public List<OnboardingEmailLog> getAllEmailLogs() {
        try {
            return onboardingEmailLogRepository.findAllByOrderByStartedAtDesc();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Object> getOnboardingStatistics() {
        try {
            List<OnboardingEmailLog> allLogs = onboardingEmailLogRepository.findAll();
            
            long totalBatches = allLogs.size();
            long completedBatches = allLogs.stream().filter(log -> "COMPLETED".equals(log.getStatus())).count();
            long failedBatches = allLogs.stream().filter(log -> "FAILED".equals(log.getStatus())).count();
            long totalEmailsSent = allLogs.stream().mapToInt(OnboardingEmailLog::getEmailsSent).sum();
            long totalEmailsFailed = allLogs.stream().mapToInt(OnboardingEmailLog::getEmailsFailed).sum();
            long pendingBatches = allLogs.stream().filter(log -> "PROCESSING".equals(log.getStatus())).count();
            
            double successRate = totalBatches > 0 ? (double) completedBatches / totalBatches * 100 : 0;
            double emailSuccessRate = (totalEmailsSent + totalEmailsFailed) > 0 ? 
                (double) totalEmailsSent / (totalEmailsSent + totalEmailsFailed) * 100 : 0;
            
            return Map.of(
                "totalOnboardingBatches", totalBatches,
                "completedBatches", completedBatches,
                "failedBatches", failedBatches,
                "pendingBatches", pendingBatches,
                "totalEmailsSent", totalEmailsSent,
                "totalEmailsFailed", totalEmailsFailed,
                "batchSuccessRate", String.format("%.1f%%", successRate),
                "emailSuccessRate", String.format("%.1f%%", emailSuccessRate),
                "lastUpdated", LocalDateTime.now()
            );
        } catch (Exception e) {
            return Map.of(
                "totalOnboardingBatches", 0,
                "completedBatches", 0,
                "failedBatches", 0,
                "pendingBatches", 0,
                "totalEmailsSent", 0,
                "totalEmailsFailed", 0,
                "batchSuccessRate", "0%",
                "emailSuccessRate", "0%",
                "error", e.getMessage()
            );
        }
    }
    
    @Override
    public List<OnboardingEmailLog> getEmailLogsByBatchId(Long batchId) {
        try {
            return onboardingEmailLogRepository.findByEmployeeBatchLogIdOrderByStartedAtDesc(batchId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    
    @Async
    @Override
    public void resendFailedEmails(Long logId) {
        try {
            OnboardingEmailLog originalLog = onboardingEmailLogRepository.findById(logId)
                    .orElseThrow(() -> new RuntimeException("Email log not found"));
            
            OnboardingEmailLog resendLog = new OnboardingEmailLog();
            resendLog.setEmployeeBatchLogId(originalLog.getEmployeeBatchLogId());
            resendLog.setStatus("PROCESSING");
            resendLog.setTotalEmployees(originalLog.getEmailsFailed());
            resendLog = onboardingEmailLogRepository.save(resendLog);
            
            try {
                List<String> failedEmails = objectMapper.readValue(originalLog.getFailedEmailsList(), List.class);
                List<String> resentEmails = new ArrayList<>();
                List<String> stillFailedEmails = new ArrayList<>();
                
                int resentCount = 0;
                int stillFailedCount = 0;
                
                for (String email : failedEmails) {
                    Employee employee = employeeRepository.findByEmail(email).orElse(null);
                    if (employee != null) {
                        boolean emailSent = sendWelcomeEmail(employee);
                        if (emailSent) {
                            resentCount++;
                            resentEmails.add(email);
                        } else {
                            stillFailedCount++;
                            stillFailedEmails.add(email);
                        }
                    }
                }
                
                resendLog.setSentEmailsList(objectMapper.writeValueAsString(resentEmails));
                resendLog.setFailedEmailsList(objectMapper.writeValueAsString(stillFailedEmails));
                resendLog.setIneligibleEmailsList("[]");
                resendLog.setDateErrorEmailsList("[]");
                resendLog.setEmailsSent(resentCount);
                resendLog.setEmailsFailed(stillFailedCount);
                resendLog.setIneligibleDueToDate(0);
                resendLog.setDateFormatErrors(0);
                resendLog.setStatus("COMPLETED");
                resendLog.setCompletedAt(LocalDateTime.now());
                
                onboardingEmailLogRepository.save(resendLog);
                
                log.info("Resend completed: {} emails resent, {} still failed", resentCount, stillFailedCount);
                
            } catch (Exception e) {
                resendLog.setStatus("FAILED");
                onboardingEmailLogRepository.save(resendLog);
                throw e;
            }
            
        } catch (Exception e) {
            log.error("Failed to resend emails for log {}: {}", logId, e.getMessage());
        }
    }
}
