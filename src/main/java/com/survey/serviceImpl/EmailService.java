//package com.survey.serviceImpl;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.context.Context;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.InternetAddress;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//    private final TemplateEngine templateEngine;
//
//    @Value("${spring.mail.username}")
//    private String from;
//
//   
//    public void sendEmailWithTemplate(String to, String subject, String templateName, Context context) {
//        try {
//            String htmlBody = templateEngine.process(templateName, context);
//
//            // Create a MIME message
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setFrom(from);
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(htmlBody, true); 
//
//            mailSender.send(message);
//            System.out.println("✅ Email sent successfully to " + to + " using template: " + templateName);
//
//        } catch (MessagingException e) {
//            System.err.println("❌ Failed to send email: " + e.getMessage());
//            throw new RuntimeException("Failed to send email to " + to, e);
//        }
//    }
//    
//    public void sendSimpleMail(String to, String subject, String text) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            message.setFrom(new InternetAddress(from));
//            message.setRecipients(MimeMessage.RecipientType.TO, to);
//            message.setSubject(subject);
//            message.setText(text);
//            mailSender.send(message);
//        } catch (Exception e) {
//            throw new RuntimeException("Email sending failed");
//        }
//    }
//
//}

package com.survey.serviceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;

	@Value("${spring.mail.username}")
	private String from;

//    public void sendEmailWithTemplate(String to, String subject, String templateName, Context context) {
//        try {
//            String htmlBody = templateEngine.process(templateName, context);
//
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setFrom(from);
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(htmlBody, true);
//
//            // ⭐ ONLY FOR ONBOARDING WELCOME TEMPLATE ⭐
//            if ("onboarding-welcome".equals(templateName)) {
//                helper.addInline("bannerImage",
//                        new ClassPathResource("static/images/Aurionpro_Welcome.png"));
//            }
//
//            mailSender.send(message);
//            System.out.println("✅ Email sent successfully to " + to + " using template: " + templateName);
//
//        } catch (MessagingException e) {
//            System.err.println("❌ Failed to send email: " + e.getMessage());
//            throw new RuntimeException("Failed to send email to " + to, e);
//        }
//    }

	public void sendEmailWithTemplate(String to, String subject, String templateName, Context context) {
		try {
			System.out.println("📨 Preparing email for: " + to);

			String htmlBody = templateEngine.process(templateName, context);
			System.out.println("📄 Template processed: " + templateName);

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlBody, true);

			if ("onboarding-welcome".equals(templateName)) {
				System.out.println("Adding inline image (CID)...");
				helper.addInline("bannerImage", new ClassPathResource("static/images/Aurionpro_Welcome.png"));
				System.out.println("Inline image added successfully.");
			}

			System.out.println("Sending email...");
			mailSender.send(message);

			System.out.println("Email SENT SUCCESSFULLY → " + to);

		} catch (MessagingException e) {
			System.err.println("EMAIL FAILED for " + to + ": " + e.getMessage());
			throw new RuntimeException("Failed to send email to " + to, e);
		}
	}

	public void sendSimpleMail(String to, String subject, String text) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			message.setFrom(new InternetAddress(from));
			message.setRecipients(MimeMessage.RecipientType.TO, to);
			message.setSubject(subject);
			message.setText(text);
			mailSender.send(message);
		} catch (Exception e) {
			throw new RuntimeException("Email sending failed");
		}
	}

}
