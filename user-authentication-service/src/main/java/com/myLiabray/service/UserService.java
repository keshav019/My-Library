package com.myLiabray.service;

import com.myLiabray.dto.LoginDto;
import com.myLiabray.dto.LoginResponseDto;
import com.myLiabray.dto.RegisterDto;
import com.myLiabray.dto.RegisterResponseDto;
import com.myLiabray.exception.TokenExpiredException;
import com.myLiabray.exception.UserAlreadyExistException;
import com.myLiabray.exception.UserNotExistException;


public interface UserService {
  public RegisterResponseDto register(RegisterDto registerDto) throws UserAlreadyExistException;
  public LoginResponseDto login(LoginDto loginDto) throws UserNotExistException;
  public String verifyEmail(String email,String token) throws UserNotExistException,TokenExpiredException;
  public RegisterResponseDto update(RegisterDto registerDto)throws UserNotExistException;
  public String forgotPassword(String email) throws UserNotExistException;
  public String resetPassword(String password,String email,String otp)throws UserNotExistException;
}
