package com.myLiabray.dto;

import com.myLiabray.model.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponseDto {
	private long userId;
	private String email;
	private String firstname;
	private String lastname;
	
	public RegisterResponseDto(User user) {
		this.userId=user.getUserId();
		this.email=user.getEmail();
		this.firstname=user.getFirstname();
		this.lastname=user.getLastname();
	}
}
