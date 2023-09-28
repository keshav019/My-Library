package com.myLiabray.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.myLiabray.dto.LoginDto;
import com.myLiabray.dto.LoginResponseDto;
import com.myLiabray.dto.MailDto;
import com.myLiabray.dto.RegisterDto;
import com.myLiabray.dto.RegisterResponseDto;
import com.myLiabray.exception.TokenExpiredException;
import com.myLiabray.exception.UserAlreadyExistException;
import com.myLiabray.exception.UserNotExistException;
import com.myLiabray.model.User;
import com.myLiabray.repository.UserRepository;
import com.myLiabray.utils.OTPGenerator;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private KafkaTemplate<String, MailDto> kafkaTemplate;
	private final UserRepository userRepository;

	@Autowired
	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public RegisterResponseDto register(RegisterDto registerDto) throws UserAlreadyExistException {
		User existingUser = userRepository.findByEmail(registerDto.getEmail()).orElse(null);
		if (existingUser == null) {
			User user = new User(registerDto);
			user.setEnable(false);
			user.setCreatedTimeStamp(LocalDateTime.now());
			user.setUpdateTimeStamp(LocalDateTime.now());
			user = userRepository.save(user);
			String otp = OTPGenerator.generateOTP();
			user.setToken(otp);
			MailDto mail = new MailDto();
			mail.otpMail(user.getEmail(), otp, user.getFirstname());
			kafkaTemplate.send("registration", mail);
			return new RegisterResponseDto(user);

		} else if (!existingUser.getEnable()) {
			existingUser.setUpdateTimeStamp(LocalDateTime.now());
			String otp = OTPGenerator.generateOTP();
			existingUser.setToken(otp);
			MailDto mail = new MailDto();
			mail.otpMail(existingUser.getEmail(), otp, existingUser.getFirstname());
			kafkaTemplate.send("registration", mail);
			return new RegisterResponseDto(existingUser);
		} else {
			throw new UserAlreadyExistException("Email already Exist");
		}

	}

	@Override
	public LoginResponseDto login(LoginDto loginDto) throws UserNotExistException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String verifyEmail(String email, String token) throws UserNotExistException, TokenExpiredException {
		User user = userRepository.findByEmail(email).orElse(null);
		if (user == null) {
			throw new UserNotExistException("User Not Exist with email: " + email);
		}
		if (user.isTokenExpired()) {
			throw new TokenExpiredException("Token Expired !");
		}
		if (user.getToken() != null && user.getToken().equals(token)) {
			throw new TokenExpiredException("Please Enter correct OTP !");
		}
		user.setEnable(true);
		MailDto mail = new MailDto();
		mail.registrationMail(user.getEmail(), user.getFirstname());
		kafkaTemplate.send("registration", mail);
		return "Email verified !";
	}

	@Override
	public RegisterResponseDto update(RegisterDto registerDto) throws UserNotExistException {
		User user = userRepository.findByEmail(registerDto.getEmail()).orElse(null);
		if (user == null) {
			throw new UserNotExistException("User Not Exist with email: " + registerDto.getEmail());
		}
		user.updateUser(registerDto);
		user = userRepository.save(user);
		return new RegisterResponseDto(user);
	}

	@Override
	public String forgotPassword(String email) throws UserNotExistException {
		User user = userRepository.findByEmail(email).orElse(null);
		if (user == null) {
			throw new UserNotExistException("User Not Exist with email: " + email);
		}
		String otp = OTPGenerator.generateOTP();
		MailDto mail = new MailDto();
		mail.otpMail(user.getEmail(), otp, user.getFirstname());
		kafkaTemplate.send("registration", mail);
		return "Otp Email Sent !";
	}

	@Override
	public String resetPassword(String password, String email, String otp) throws UserNotExistException {
		// TODO Auto-generated method stub
		return "Password Updated";
	}

}
