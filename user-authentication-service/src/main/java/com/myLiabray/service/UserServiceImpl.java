package com.myLiabray.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myLiabray.dto.LoginDto;
import com.myLiabray.dto.LoginResponseDto;
import com.myLiabray.dto.RegisterDto;
import com.myLiabray.dto.RegisterResponseDto;
import com.myLiabray.exception.TokenExpiredException;
import com.myLiabray.exception.UserAlreadyExistException;
import com.myLiabray.exception.UserNotExistException;
import com.myLiabray.model.User;
import com.myLiabray.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	@Autowired
	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public RegisterResponseDto register(RegisterDto registerDto) throws UserAlreadyExistException {
		if (userRepository.existByEmail(registerDto.getEmail())) {
			throw new UserAlreadyExistException("Email already Exist");
		}
		User user = new User(registerDto);
		user.setEnable(false);
		user.setCreatedTimeStamp(LocalDateTime.now());
		user.setUpdateTimeStamp(LocalDateTime.now());
		user = userRepository.save(user);
		return new RegisterResponseDto(user);
	}

	@Override
	public LoginResponseDto login(LoginDto loginDto) throws UserNotExistException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String verifyEmail(String email, String token) throws UserNotExistException,TokenExpiredException {
		User user = userRepository.findByEmail(email).orElse(null);
		if (user == null) {
			throw new UserNotExistException("User Not Exist with email: " + email);
		}
		if(user.isTokenExpired()) {
			throw new TokenExpiredException("Token Expired !");
		}
		if(user.getToken()!=null && user.getToken().equals(token)) {
			throw new TokenExpiredException("Please Enter correct OTP !");
		}
		user.setEnable(true);
		return "Email verified !";
	}

	@Override
	public String sendToken(String email) throws UserNotExistException {
		
		return "Email Sent !";
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
		
		return "Email sent";
	}

	@Override
	public String resetPassword(String password, String email, String otp) throws UserNotExistException {
		// TODO Auto-generated method stub
		return "Password Updated";
	}

}
