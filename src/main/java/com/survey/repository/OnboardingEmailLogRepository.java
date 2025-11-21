//package com.survey.repository;
//
//import com.survey.entity.OnboardingEmailLog;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface OnboardingEmailLogRepository extends JpaRepository<OnboardingEmailLog, Long> {
//    
//    OnboardingEmailLog findByEmployeeBatchLogId(Long employeeBatchLogId);
//    
//    void sendWelcomeEmailsForBatch(Long employeeBatchLogId);
//}


package com.survey.repository;

import com.survey.entity.OnboardingEmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OnboardingEmailLogRepository extends JpaRepository<OnboardingEmailLog, Long> {
    
    // ✅ Find by employee batch log ID
    OnboardingEmailLog findByEmployeeBatchLogId(Long employeeBatchLogId);
    
    // ✅ Get all logs ordered by start date (descending)
    List<OnboardingEmailLog> findAllByOrderByStartedAtDesc();
    
    // ✅ Find all logs for a specific batch ID ordered by start date
    List<OnboardingEmailLog> findByEmployeeBatchLogIdOrderByStartedAtDesc(Long employeeBatchLogId);
}