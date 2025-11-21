//package com.survey.service;
//
//public interface OnboardingEmailService {
//    void sendWelcomeEmailsForBatch(Long employeeBatchLogId);
//    //boolean sendWelcomeEmailToEmployee(String employeeEmail, String employeeName, String employeeId);
//    
//    
//}



package com.survey.service;

import com.survey.entity.OnboardingEmailLog;
import java.util.List;
import java.util.Map;

public interface OnboardingEmailService {
    
    void sendWelcomeEmailsForBatch(Long employeeBatchLogId);
    
    Boolean isEmployeeEligibleForOnboarding(String employeeEmail);
    
    Map<String, Object> getEmailResults(Long logId);
    
    List<OnboardingEmailLog> getAllEmailLogs();
    
    Map<String, Object> getOnboardingStatistics();
    
    List<OnboardingEmailLog> getEmailLogsByBatchId(Long batchId);
    
    void resendFailedEmails(Long logId);
}