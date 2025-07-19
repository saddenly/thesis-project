package com.rustem.eduthesis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rustem.eduthesis.api.controller.AuthController;
import com.rustem.eduthesis.api.dto.LoginRequest;
import com.rustem.eduthesis.api.dto.SignupRequest;
import com.rustem.eduthesis.api.dto.UserResponse;
import com.rustem.eduthesis.infrastructure.entity.UserEntity;
import com.rustem.eduthesis.infrastructure.exception.EmailAlreadyExistsException;
import com.rustem.eduthesis.infrastructure.exception.GlobalExceptionHandler;
import com.rustem.eduthesis.infrastructure.mapper.UserMapper;
import com.rustem.eduthesis.infrastructure.security.jwt.JwtTokenProvider;
import com.rustem.eduthesis.infrastructure.service.AuthenticationService;
import com.rustem.eduthesis.infrastructure.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationService authService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    private UserEntity registeredUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("password123");

        signupRequest = new SignupRequest();
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFirstName("New");
        signupRequest.setLastName("User");

        registeredUser = new UserEntity();
        registeredUser.setId(1L);
        registeredUser.setEmail("newuser@example.com");
        registeredUser.setFirstName("New");
        registeredUser.setLastName("User");
    }

    @Test
    void authenticateUser_withValidCredentials_shouldReturnToken() throws Exception {
        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.createToken(authentication)).thenReturn("mocked.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).createToken(any(Authentication.class));
    }

    @Test
    void authenticateUser_withInvalidCredentials_shouldReturnBadRequest() throws Exception {
        LoginRequest emptyLoginRequest = new LoginRequest();

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyLoginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_withValidCredentials_shouldReturnToken() throws Exception {
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("newuser@example.com")
                .firstName("New")
                .lastName("User")
                .build();

        when(authService.registerUser(any(SignupRequest.class))).thenReturn(registeredUser);
        when(userMapper.toResponse(any(UserEntity.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(registeredUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(registeredUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(registeredUser.getLastName()));

        verify(authService).registerUser(any(SignupRequest.class));
        verify(userMapper).toResponse(any(UserEntity.class));
    }

    @Test
    void registerUser_withInvalidCredentials_shouldReturnBadRequest() throws Exception {
        SignupRequest invalidSignupRequest = new SignupRequest();
        // Leave fields empty to trigger validation

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSignupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_withExistingEmail_shouldReturnBadRequest() throws Exception {
        when(authService.registerUser(any(SignupRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));

        verify(authService).registerUser(any(SignupRequest.class));
    }
}
