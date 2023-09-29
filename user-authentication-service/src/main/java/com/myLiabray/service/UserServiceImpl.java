package com.myLiabray.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private KafkaTemplate<String, String> kafkaTemplate;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private JwtService jwtService;
	@Autowired
	private AuthenticationManager authenticationManager;


	@Override
	public RegisterResponseDto register(RegisterDto registerDto) throws UserAlreadyExistException {
		User existingUser = userRepository.findByEmail(registerDto.getEmail()).orElse(null);
		if (existingUser == null) {
			registerDto.setPassword(passwordEncoder.encode(registerDto.getPassword()));
			User user = new User(registerDto);
			user.setEnable(false);
			user.setCreatedTimeStamp(LocalDateTime.now());
			user.setUpdateTimeStamp(LocalDateTime.now());
			String otp = OTPGenerator.generateOTP();
			user.setToken(otp);
			user = userRepository.save(user);
			MailDto mail = new MailDto();
			mail.otpMail(user.getEmail(), otp, user.getFirstname());
			
			ObjectMapper objectMapper = new ObjectMapper();
			String message;
			try {
				message = objectMapper.writeValueAsString(mail);
				kafkaTemplate.send("registration", message);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new RegisterResponseDto(user);

		} else if (!existingUser.verified()) {
			existingUser.setUpdateTimeStamp(LocalDateTime.now());
			existingUser.setPassword(passwordEncoder.encode(registerDto.getPassword()));
			existingUser.setFirstname(registerDto.getFirstname());
			existingUser.setLastname(registerDto.getLastname());
			String otp = OTPGenerator.generateOTP();
			existingUser.setToken(otp);
			userRepository.save(existingUser);
			MailDto mail = new MailDto();
			mail.otpMail(existingUser.getEmail(), otp, existingUser.getFirstname());
			ObjectMapper objectMapper = new ObjectMapper();
			String message;
			try {
				message = objectMapper.writeValueAsString(mail);
				kafkaTemplate.send("registration", message);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new RegisterResponseDto(existingUser);
		} else {
			throw new UserAlreadyExistException("Email already Exist");
		}

	}

	@Override
	public LoginResponseDto login(LoginDto loginDto) throws UserNotExistException {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
		UserDetails user = (UserDetails) authentication.getPrincipal();
		String jwtToken = jwtService.generateToken(user);
		LoginResponseDto loginResponse = new LoginResponseDto();
		loginResponse.setEmail(loginDto.getEmail());
		loginResponse.setToken(jwtToken);
		return loginResponse;
	}

	@Override
	public String verifyEmail(String email, String token) throws UserNotExistException, TokenExpiredException {
		User user = userRepository.findByEmail(email).orElse(null);
		if (user == null) {
			throw new UserNotExistException("User Not Exist with email: " + email);
		}
		if (user.isTokenExpired()) {
			throw new TokenExpiredException("OTP Expired !");
		}

		if (user.getToken() == null || user.getToken().equals(token)) {
			throw new TokenExpiredException("Please Enter correct OTP !");
		}
		user.setEnable(true);
		user.setToken(null);
		user = userRepository.save(user);
		MailDto mail = new MailDto();
		mail.registrationMail(user.getEmail(), user.getFirstname());
		ObjectMapper objectMapper = new ObjectMapper();
		String message;
		try {
			message = objectMapper.writeValueAsString(mail);
			kafkaTemplate.send("registration", message);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		user.setToken(otp);
		user.setUpdateTimeStamp(LocalDateTime.now());
		user = userRepository.save(user);
		MailDto mail = new MailDto();
		mail.otpMail(user.getEmail(), otp, user.getFirstname());
		ObjectMapper objectMapper = new ObjectMapper();
		String message;
		try {
			message = objectMapper.writeValueAsString(mail);
			kafkaTemplate.send("registration", message);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Otp Email Sent !";
	}

	@Override
	public String resetPassword(String password, String email, String otp) throws UserNotExistException, TokenExpiredException {
		User user = userRepository.findByEmail(email).orElse(null);
		if (user == null) {
			throw new UserNotExistException("User Not Exist with email: " + email);
		}
		if (user.isTokenExpired()) {
			throw new TokenExpiredException("OTP Expired !");
		}
		if (user.getToken() == null || !user.getToken().equals(otp)) {
			throw new TokenExpiredException("Please Enter correct OTP !");
		}
		user.setEnable(true);
		user.setPassword(passwordEncoder.encode(password));
		userRepository.save(user);
		return "Password Updated";
	}

}
