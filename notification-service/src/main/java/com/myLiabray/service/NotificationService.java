package com.myLiabray.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
	private final JavaMailSender mailSender;
	 
	 public NotificationService(JavaMailSender mailSender) {
			super();
			this.mailSender = mailSender;
		}
	 @KafkaListener(topics = "registration", groupId = "notification")
	 @KafkaListener(topics = "forgotpassword", groupId = "notification")
	 public void sendEmail(String email, String subject, String emailBody) {
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
			simpleMailMessage.setFrom("parkwavez@gmail.com");
			simpleMailMessage.setTo(email);
			simpleMailMessage.setSubject(subject);
			simpleMailMessage.setText(emailBody);
			this.mailSender.send(simpleMailMessage);	
		}
}
