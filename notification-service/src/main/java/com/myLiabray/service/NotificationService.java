package com.myLiabray.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myLiabray.dto.MailDto;

@Service
public class NotificationService {
	private final JavaMailSender mailSender;
	 
	 public NotificationService(JavaMailSender mailSender) {
			super();
			this.mailSender = mailSender;
		}
	 @KafkaListener(topics = "registration", groupId = "notification")
	 @KafkaListener(topics = "forgotpassword", groupId = "notification")
	 public void sendEmail(String message) throws JsonMappingException, JsonProcessingException {
		 ObjectMapper objectMapper = new ObjectMapper();
		 MailDto mailDto=objectMapper.readValue(message, MailDto.class);
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
			simpleMailMessage.setFrom("viviane.predovic@ethereal.email");
			simpleMailMessage.setTo(mailDto.getEmail());
			simpleMailMessage.setSubject(mailDto.getSubject());
			simpleMailMessage.setText(mailDto.getEmailBody());
			this.mailSender.send(simpleMailMessage);	
		}
}
