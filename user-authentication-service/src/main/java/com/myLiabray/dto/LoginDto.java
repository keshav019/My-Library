package com.myLiabray.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email")
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Invalid Password")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).*$", message = "Invalid Password")
	private String password;
}
