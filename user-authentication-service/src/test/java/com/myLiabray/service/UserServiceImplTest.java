package com.myLiabray.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.SettableListenableFuture;

import com.myLiabray.dto.LoginDto;
import com.myLiabray.dto.LoginResponseDto;
import com.myLiabray.dto.RegisterDto;
import com.myLiabray.dto.RegisterResponseDto;
import com.myLiabray.dto.MailDto;
import com.myLiabray.exception.TokenExpiredException;
import com.myLiabray.exception.UserAlreadyExistException;
import com.myLiabray.exception.UserNotExistException;
import com.myLiabray.model.User;
import com.myLiabray.repository.UserRepository;
import com.myLiabray.utils.OTPGenerator;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, MailDto> kafkaTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testRegisterNewUser() throws UserAlreadyExistException {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail("newuser@example.com");
        registerDto.setFirstname("keshav");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(kafkaTemplate).send(anyString(), any(MailDto.class));

        RegisterResponseDto response = userService.register(registerDto);

        verify(userRepository).findByEmail("newuser@example.com");
        verify(userRepository).save(any(User.class));
        verify(kafkaTemplate).send("registration", any(MailDto.class));

        assertNotNull(response);
        assertEquals("keshav", response.getFirstname());
    }

    @Test
    void testRegisterExistingUser() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail("existinguser@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
        assertThrows(UserAlreadyExistException.class, () -> {
            userService.register(registerDto);
        });

        verify(userRepository).findByEmail("existinguser@example.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    void testRegisterExistingUserNotEnabled() throws UserAlreadyExistException {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail("existinguser@example.com");
        registerDto.setFirstname("keshav");
        User existingUser = new User();
        existingUser.setEnable(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        SettableListenableFuture<SendResult<String, MailDto>> future = new SettableListenableFuture<>();
        when(kafkaTemplate.send(anyString(), any(MailDto.class))).thenReturn(future);

        RegisterResponseDto response = userService.register(registerDto);

        verify(userRepository).findByEmail("existinguser@example.com");
        verify(userRepository).save(existingUser);
        verify(kafkaTemplate).send("registration", any(MailDto.class));

        assertNotNull(response);
        assertEquals("keshav", response.getFirstname()); 
    }

    @Test
    void testLogin() throws UserNotExistException {
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail("user@example.com");

        User existingUser = new User();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));

        LoginResponseDto response = userService.login(loginDto);

        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void testLoginUserNotExist() {
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail("nonexistinguser@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class, () -> {
            userService.login(loginDto);
        });

        verify(userRepository).findByEmail("nonexistinguser@example.com");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testVerifyEmail() throws UserNotExistException, TokenExpiredException {
        String email = "user@example.com";
        String token = "123456";

        User existingUser = new User();
        existingUser.setEmail(email);
        existingUser.setToken(token);
        existingUser.setEnable(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(OTPGenerator.generateOTP()).thenReturn(token);

        String result = userService.verifyEmail(email, token);

        verify(userRepository).findByEmail(email);

        assertTrue(existingUser.verified()); // Check if user is enabled

        assertNotNull(result);
        // Add more assertions for the result, email sending, etc.
    }

    @Test
    void testVerifyEmailUserNotExist() {
        String email = "nonexistinguser@example.com";
        String token = "123456";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class, () -> {
            userService.verifyEmail(email, token);
        });

        verify(userRepository).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    void testVerifyEmailTokenExpired() {
        String email = "user@example.com";
        String token = "123456";

        User existingUser = new User();
        existingUser.setEmail(email);
        existingUser.setToken(token);
        existingUser.setEnable(false);
        existingUser.setUpdateTimeStamp(LocalDateTime.now().minusHours(2)); // Simulate an expired token

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        assertThrows(TokenExpiredException.class, () -> {
            userService.verifyEmail(email, token);
        });

        verify(userRepository).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    void testUpdate() throws UserNotExistException {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail("user@example.com");

        User existingUser = new User();
        existingUser.setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponseDto response = userService.update(registerDto);

        verify(userRepository).findByEmail("user@example.com");
        verify(userRepository).save(existingUser);

        assertNotNull(response);
        // Add assertions for the response
    }

    @Test
    void testUpdateUserNotExist() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail("nonexistinguser@example.com");

        when(userRepository.findByEmail("nonexistinguser@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class, () -> {
            userService.update(registerDto);
        });

        verify(userRepository).findByEmail("nonexistinguser@example.com");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testForgotPassword() throws UserNotExistException {
        String email = "user@example.com";

        User existingUser = new User();
        existingUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        SettableListenableFuture<SendResult<String, MailDto>> future = new SettableListenableFuture<>();
        when(kafkaTemplate.send(anyString(), any(MailDto.class))).thenReturn(future);
        String result = userService.forgotPassword(email);

        verify(userRepository).findByEmail(email);
        verify(kafkaTemplate).send("registration", any(MailDto.class));

        assertNotNull(result);
        // Add assertions for the result
    }

    @Test
    void testForgotPasswordUserNotExist() {
        String email = "nonexistinguser@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class, () -> {
            userService.forgotPassword(email);
        });

        verify(userRepository).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    void testResetPassword() throws UserNotExistException, TokenExpiredException {
        String email = "user@example.com";
        String newPassword = "newPassword";
        String otp = "123456";

        User existingUser = new User();
        existingUser.setEmail(email);
        existingUser.setToken(otp);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        String result = userService.resetPassword(newPassword, email, otp);

        verify(userRepository).findByEmail(email);

        assertNotNull(result);
    }
}


