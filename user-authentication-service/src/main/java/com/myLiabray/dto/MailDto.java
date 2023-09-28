package com.myLiabray.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MailDto {
	private String email;
	private String subject;
	private String emailBody;
	
	public void otpMail(String email,String otp,String name) {
		this.email=email;
		this.subject="Email verification";
		this.emailBody="Hello "+name +"\n"+ "Otp to verify your Email is : "+ otp;
		
	}
	public void registrationMail(String email,String name) {
		this.email=email;
		this.subject="Registration";
		this.emailBody="Hello "+name +"\n"+ "Your Accout is created sucessfully : ";
	}
}
