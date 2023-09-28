package com.myLiabray.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.myLiabray.dto.RegisterDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long userId;
	private String email;
	private String firstname;
	private String lastname;
	private String password;
	private boolean enable;
	private String token;
	private LocalDateTime createdTimeStamp;
	private LocalDateTime updateTimeStamp;
	
	public User(RegisterDto registerDto) {
		this.email=registerDto.getEmail();
		this.firstname=registerDto.getFirstname();
		this.lastname=registerDto.getLastname();
		this.password=registerDto.getPassword();
	}
	
	public void updateUser(RegisterDto registerDto) {
		this.firstname=registerDto.getFirstname();
		this.lastname=registerDto.getLastname();
	}
	
	public boolean isTokenExpired() {
	    LocalDateTime currentTime = LocalDateTime.now();
	    long minutesDifference = ChronoUnit.MINUTES.between(updateTimeStamp, currentTime);
	    return minutesDifference >= 10;
	}

}
