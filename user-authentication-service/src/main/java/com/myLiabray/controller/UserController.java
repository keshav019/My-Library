package com.myLiabray.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myLiabray.dto.LoginDto;
import com.myLiabray.dto.LoginResponseDto;
import com.myLiabray.dto.RegisterDto;
import com.myLiabray.dto.RegisterResponseDto;
import com.myLiabray.dto.ResetPasswordDto;
import com.myLiabray.exception.TokenExpiredException;
import com.myLiabray.exception.UserAlreadyExistException;
import com.myLiabray.exception.UserNotExistException;
import com.myLiabray.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {
	@Autowired
	private UserService userService;
	
	@PostMapping("/register")
	public ResponseEntity<?> signUp(@RequestBody @Valid RegisterDto registerDto) throws UserAlreadyExistException {
		RegisterResponseDto signupResponseDto = userService.register(registerDto);
		return ResponseEntity.ok(signupResponseDto);
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody @Valid LoginDto loginDto) throws UserNotExistException {
		LoginResponseDto loginResponseDto = userService.login(loginDto);
		return ResponseEntity.ok(loginResponseDto);
	}
	
	@PostMapping("/verify")
	public ResponseEntity<String> verifyEmail(
            @RequestParam String email,
            @RequestParam String otp
			) throws UserNotExistException, TokenExpiredException{
		System.out.println(otp);
		String res=userService.verifyEmail(email, otp);
		return ResponseEntity.ok(res);
	}
	@PostMapping("/update")
	public ResponseEntity<?> update(@RequestBody @Valid RegisterDto registerDto) throws UserNotExistException{
		RegisterResponseDto res=userService.update(registerDto);
		return ResponseEntity.ok(res);
	}
	
	@GetMapping("/forgot")
	public ResponseEntity<?> forgotPassword(@RequestParam String email) throws UserNotExistException{
		String res=userService.forgotPassword(email);
		return ResponseEntity.ok(res);
	}
	@PostMapping("/reset")
	public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordDto resetPasswordDto) throws UserNotExistException,TokenExpiredException{
		String res=userService.resetPassword(resetPasswordDto.getPassword(), resetPasswordDto.getEmail(), resetPasswordDto.getOtp());
		return ResponseEntity.ok(res);
	}

}
